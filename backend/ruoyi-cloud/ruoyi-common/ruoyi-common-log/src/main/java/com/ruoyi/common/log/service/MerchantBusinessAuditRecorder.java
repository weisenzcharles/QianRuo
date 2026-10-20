package com.ruoyi.common.log.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.common.log.filter.PropertyPreExcludeFilter;
import com.ruoyi.merchant.api.RemoteMerchantBusinessAuditService;
import com.ruoyi.merchant.api.model.MerchantBusinessAuditCommand;

/**
 * 商户业务审计通用记录器。
 *
 * <p>业务服务只需要提交模块、动作、对象和前后对象，本记录器负责读取可信商户上下文、
 * 脱敏序列化、控制快照长度以及调用商户审计内部契约。业务写操作要求审计成功，
 * 远程服务连续失败时抛出异常并回滚业务事务，避免出现“业务已修改但日志缺失”；
 * 登录等不可回滚流程使用独立的非阻断入口。内部契约最终只写入商户独立业务审计表，
     * 不与平台请求级操作日志混用，最终由商户服务写入 {@code ry_rental_audit_log}。</p>
 */
@Service
public class MerchantBusinessAuditRecorder
{
    private static final Logger log = LoggerFactory.getLogger(MerchantBusinessAuditRecorder.class);

    private static final int CONTENT_MAX_LENGTH = 60000;

    /** 商户业务审计远程写入的最大尝试次数。 */
    private static final int SUBMIT_MAX_ATTEMPTS = 3;

    /** 商户业务审计重试的基础等待时间。 */
    private static final long SUBMIT_RETRY_DELAY_MILLIS = 150L;

    private static final String[] SENSITIVE_PROPERTIES = {
            "password", "oldPassword", "newPassword", "confirmPassword", "token", "accessToken",
            "refreshToken", "authorization", "phone", "phonenumber", "mobile", "email", "contactPhone",
            "servicePhone", "contactEmail", "alipayAccount", "idCard", "idNo", "licenseNo"
    };

    private final ObjectProvider<RemoteMerchantBusinessAuditService> remoteAuditServiceProvider;

    /** 注入商户业务审计远程契约。 */
    public MerchantBusinessAuditRecorder(ObjectProvider<RemoteMerchantBusinessAuditService> remoteAuditServiceProvider)
    {
        this.remoteAuditServiceProvider = remoteAuditServiceProvider;
    }

    /**
     * 记录当前已登录商户的一次业务变更。
     *
     * @param module 业务模块名称
     * @param operationType 操作类型编码
     * @param targetType 操作对象类型
     * @param targetId 操作对象唯一编号或业务编码
     * @param targetName 操作对象名称
     * @param description 业务描述
     * @param before 变更前对象
     * @param after 变更后对象
     */
    public void record(String module, String operationType, String targetType, String targetId,
            String targetName, String description, Object before, Object after)
    {
        if (!MerchantContextUtils.isMerchantUser())
        {
            log.debug("跳过商户业务审计：当前请求不是商户用户上下文，模块={}, 操作={}", module, operationType);
            return;
        }
        MerchantBusinessAuditCommand command = new MerchantBusinessAuditCommand();
        command.setTenantId(MerchantContextUtils.getTenantId());
        command.setMemberId(MerchantContextUtils.getMemberId());
        command.setOperatorUserId(SecurityUtils.getUserId());
        command.setOperatorName(limit(SecurityUtils.getUsername(), 64));
        command.setBusinessModule(limit(module, 64));
        command.setOperationType(limit(operationType, 32));
        command.setTargetType(limit(targetType, 64));
        command.setTargetId(limit(targetId, 128));
        command.setTargetName(limit(targetName, 255));
        command.setDescription(limit(description, 1000));
        command.setBeforeContent(snapshot(before));
        command.setAfterContent(snapshot(after));
        command.setBeforeDisplayContent(MerchantAuditDisplaySnapshot.sanitizeJson(command.getBeforeContent()));
        command.setAfterDisplayContent(MerchantAuditDisplaySnapshot.sanitizeJson(command.getAfterContent()));
        command.setIpAddress(IpUtils.getIpAddr());
        command.setStatus(0);
        submit(command, true);
    }

    /**
     * 记录带名称解析展示快照的商户业务变更。
     *
     * <p>原始对象仍写入内部审计快照，展示对象单独写入展示快照。两者分开保存，既保留追溯能力，
     * 又避免商户页面依赖当前下拉数据或看到主键、外键。</p>
     *
     * @param module 业务模块名称
     * @param operationType 操作类型编码
     * @param targetType 操作对象类型
     * @param targetId 操作对象唯一编号或业务编码
     * @param targetName 操作对象名称
     * @param description 业务描述
     * @param before 变更前原始对象
     * @param after 变更后原始对象
     * @param beforeDisplay 变更前展示对象
     * @param afterDisplay 变更后展示对象
     */
    public void record(String module, String operationType, String targetType, String targetId,
            String targetName, String description, Object before, Object after,
            Object beforeDisplay, Object afterDisplay)
    {
        if (!MerchantContextUtils.isMerchantUser())
        {
            log.debug("跳过商户业务审计：当前请求不是商户用户上下文，模块={}, 操作={}", module, operationType);
            return;
        }
        MerchantBusinessAuditCommand command = new MerchantBusinessAuditCommand();
        command.setTenantId(MerchantContextUtils.getTenantId());
        command.setMemberId(MerchantContextUtils.getMemberId());
        command.setOperatorUserId(SecurityUtils.getUserId());
        command.setOperatorName(limit(SecurityUtils.getUsername(), 64));
        command.setBusinessModule(limit(module, 64));
        command.setOperationType(limit(operationType, 32));
        command.setTargetType(limit(targetType, 64));
        command.setTargetId(limit(targetId, 128));
        command.setTargetName(limit(targetName, 255));
        command.setDescription(limit(description, 1000));
        command.setBeforeContent(snapshot(before));
        command.setAfterContent(snapshot(after));
        command.setBeforeDisplayContent(MerchantAuditDisplaySnapshot.sanitizeJson(snapshot(beforeDisplay)));
        command.setAfterDisplayContent(MerchantAuditDisplaySnapshot.sanitizeJson(snapshot(afterDisplay)));
        command.setIpAddress(IpUtils.getIpAddr());
        command.setStatus(0);
        submit(command, true);
    }

    /**
     * 提交一条已经包含可信归属信息的审计命令。
     *
     * <p>业务变更必须使用严格模式；远程审计连续失败时抛出异常，由上层事务回滚主业务。</p>
     *
     * @param command 已填充归属和操作者的审计命令
     */
    public void record(MerchantBusinessAuditCommand command)
    {
        if (command == null)
        {
            return;
        }
        normalizeCommand(command);
        submit(command, true);
    }

    /**
     * 非阻断记录认证类审计，例如登录成功。
     *
     * <p>认证请求完成后已经生成会话，不能用审计服务的短暂故障阻断登录；该入口仍然执行
     * 相同的重试和错误日志记录，但最终失败只记录本地错误，不抛出业务异常。</p>
     *
     * @param command 已填充可信租户、商户和操作者信息的审计命令
     */
    public void recordBestEffort(MerchantBusinessAuditCommand command)
    {
        if (command == null)
        {
            return;
        }
        normalizeCommand(command);
        submit(command, false);
    }

    /**
     * 调用商户服务内部审计接口，并按业务类型执行严格或非阻断失败策略。
     *
     * @param command 待写入审计命令
     * @param failFast true 表示最终失败时阻止主业务成功返回
     */
    private void submit(MerchantBusinessAuditCommand command, boolean failFast)
    {
        RemoteMerchantBusinessAuditService remoteAuditService = remoteAuditServiceProvider.getIfAvailable();
        if (remoteAuditService == null)
        {
            String reason = "远程审计服务未注入";
            log.error("商户业务审计写入失败：租户={}, 商户={}, 模块={}, 操作={}, 原因={}",
                    command.getTenantId(), command.getMemberId(), command.getBusinessModule(),
                    command.getOperationType(), reason);
            failIfRequired(failFast, reason);
            return;
        }

        String reason = "服务无响应";
        for (int attempt = 1; attempt <= SUBMIT_MAX_ATTEMPTS; attempt++)
        {
            try
            {
                R<Boolean> result = remoteAuditService.record(command, SecurityConstants.INNER);
                if (result != null && R.SUCCESS == result.getCode() && Boolean.TRUE.equals(result.getData()))
                {
                    log.info("商户业务审计写入成功：租户={}, 商户={}, 操作人={}, 模块={}, 操作={}, 对象={}, 尝试次数={}",
                            command.getTenantId(), command.getMemberId(), command.getOperatorUserId(),
                            command.getBusinessModule(), command.getOperationType(), command.getTargetId(), attempt);
                    return;
                }
                reason = result == null ? "服务无响应" : StringUtils.defaultIfEmpty(result.getMsg(), "服务返回失败");
            }
            catch (RuntimeException exception)
            {
                reason = StringUtils.defaultIfEmpty(exception.getMessage(), exception.getClass().getSimpleName());
                log.warn("商户业务审计调用失败：租户={}, 商户={}, 模块={}, 操作={}, 第{}次尝试，原因={}",
                        command.getTenantId(), command.getMemberId(), command.getBusinessModule(),
                        command.getOperationType(), attempt, reason);
            }
            if (attempt < SUBMIT_MAX_ATTEMPTS)
            {
                sleepBeforeRetry(attempt);
            }
        }
        log.error("商户业务审计写入最终失败：租户={}, 商户={}, 模块={}, 操作={}, 对象={}, 原因={}",
                command.getTenantId(), command.getMemberId(), command.getBusinessModule(),
                command.getOperationType(), command.getTargetId(), reason);
        failIfRequired(failFast, reason);
    }

    /**
     * 审计失败严格模式下抛出统一业务异常，保证事务不会留下未记录的业务变更。
     *
     * @param failFast 是否阻断主业务
     * @param reason 最终失败原因
     */
    private void failIfRequired(boolean failFast, String reason)
    {
        if (failFast)
        {
            throw new ServiceException("业务审计写入失败，当前业务未完成，请稍后重试：" + reason);
        }
    }

    /**
     * 在重试前短暂等待服务发现、连接池或网络恢复。
     *
     * @param attempt 已完成的尝试次数
     */
    private void sleepBeforeRetry(int attempt)
    {
        try
        {
            Thread.sleep(SUBMIT_RETRY_DELAY_MILLIS * attempt);
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            log.warn("商户业务审计重试被当前线程中断");
        }
    }

    /**
     * 统一规范内部审计命令的文本字段和快照字段，保证所有入口使用相同数据规则。
     *
     * @param command 待规范化的审计命令
     */
    private void normalizeCommand(MerchantBusinessAuditCommand command)
    {
        command.setBeforeContent(normalizeSnapshot(command.getBeforeContent()));
        command.setAfterContent(normalizeSnapshot(command.getAfterContent()));
        command.setBeforeDisplayContent(normalizeDisplaySnapshot(command.getBeforeDisplayContent(), command.getBeforeContent()));
        command.setAfterDisplayContent(normalizeDisplaySnapshot(command.getAfterDisplayContent(), command.getAfterContent()));
        command.setOperatorName(limit(command.getOperatorName(), 64));
        command.setBusinessModule(limit(command.getBusinessModule(), 64));
        command.setOperationType(limit(command.getOperationType(), 32));
        command.setTargetType(limit(command.getTargetType(), 64));
        command.setTargetId(limit(command.getTargetId(), 128));
        command.setTargetName(limit(command.getTargetName(), 255));
        command.setDescription(limit(command.getDescription(), 1000));
        command.setIpAddress(limit(command.getIpAddress(), 64));
        command.setErrorMessage(limit(command.getErrorMessage(), 2000));
        command.setTraceId(limit(command.getTraceId(), 64));
    }

    /**
     * 对业务对象执行敏感字段过滤后生成 JSON 快照。
     *
     * @param value 待序列化对象
     * @return 脱敏后的 JSON 文本
     */
    private String snapshot(Object value)
    {
        if (value == null)
        {
            return "";
        }
        try
        {
            return fitJson(JSON.toJSONString(value, new PropertyPreExcludeFilter().addExcludes(SENSITIVE_PROPERTIES)));
        }
        catch (RuntimeException exception)
        {
            log.warn("商户业务审计快照序列化失败: {}", exception.getMessage());
            return "";
        }
    }

    /**
     * 校验内部调用方传入的快照，并确保最终写入数据库的一定是合法 JSON。
     *
     * @param value 原始快照
     * @return 合法且不超过字段限制的 JSON 文本
     */
    private String normalizeSnapshot(String value)
    {
        if (value == null || value.isEmpty())
        {
            return "";
        }
        try
        {
            JSON.parse(value);
            return fitJson(value);
        }
        catch (RuntimeException exception)
        {
            return fitJson(JSON.toJSONString(value));
        }
    }

    /** 展示快照缺省时从原始快照生成安全版本，保证旧调用方也不会泄漏技术编号。 */
    private String normalizeDisplaySnapshot(String displaySnapshot, String rawSnapshot)
    {
        if (StringUtils.isEmpty(displaySnapshot))
        {
            return MerchantAuditDisplaySnapshot.sanitizeJson(rawSnapshot);
        }
        return MerchantAuditDisplaySnapshot.sanitizeJson(normalizeSnapshot(displaySnapshot));
    }

    /**
     * 对超长快照生成合法的摘要 JSON，避免直接截断 JSON 文本造成数据库解析失败。
     *
     * @param json 已序列化的 JSON 文本
     * @return 原始 JSON 或合法的截断摘要 JSON
     */
    private String fitJson(String json)
    {
        if (json == null || json.isEmpty() || json.length() <= CONTENT_MAX_LENGTH)
        {
            return json == null ? "" : json;
        }
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("truncated", true);
        summary.put("originalLength", json.length());
        summary.put("preview", json.substring(0, Math.min(50000, json.length())));
        return JSON.toJSONString(summary);
    }

    /**
     * 按数据库字段上限截断普通文本字段。
     *
     * @param value 原始文本
     * @param maxLength 字段最大长度
     * @return 限长后的文本
     */
    private String limit(String value, int maxLength)
    {
        return StringUtils.isEmpty(value) ? "" : StringUtils.substring(value, 0, maxLength);
    }
}
