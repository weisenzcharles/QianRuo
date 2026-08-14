package com.ruoyi.merchant.application;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.common.log.filter.PropertyPreExcludeFilter;
import com.ruoyi.merchant.domain.MerchantBusinessAuditRecord;
import com.ruoyi.merchant.mapper.MerchantBusinessAuditMapper;
import com.ruoyi.merchant.model.MerchantBusinessAuditQuery;
import com.ruoyi.merchant.model.MerchantBusinessAuditOptionsVO;
import com.ruoyi.merchant.model.MerchantBusinessAuditVO;
import com.ruoyi.merchant.api.model.MerchantBusinessAuditCommand;

/**
 * 商户业务审计应用服务。
 *
 * <p>该服务是商户侧所有业务变更日志的统一入口，统一读写租赁域的
 * {@code ry_rental_audit_log} 事实表。它只记录业务模块、业务动作、操作对象和前后快照；
 * 请求方式、请求地址、控制器方法名等请求级字段继续由平台的通用操作日志链路保存。</p>
 */
@Service
public class MerchantBusinessAuditService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantBusinessAuditService.class);

    /** 业务快照统一限制长度，避免单条审计记录无限膨胀。 */
    private static final int CONTENT_MAX_LENGTH = 60000;

    /** 序列化时统一排除密码、联系方式和令牌等敏感属性。 */
    private static final String[] SENSITIVE_PROPERTIES = {
            "password", "oldPassword", "newPassword", "confirmPassword", "token", "accessToken",
            "refreshToken", "authorization", "phone", "phonenumber", "mobile", "email", "contactPhone",
            "servicePhone", "contactEmail", "alipayAccount", "idCard", "idNo", "licenseNo"
    };

    private final MerchantBusinessAuditMapper auditMapper;

    /** 注入商户业务审计数据访问对象。 */
    public MerchantBusinessAuditService(MerchantBusinessAuditMapper auditMapper)
    {
        this.auditMapper = auditMapper;
    }

    /**
     * 查询当前登录商户的业务审计摘要。
     *
     * @param query 页面筛选条件
     * @return 当前商户的业务审计列表
     */
    public java.util.List<MerchantBusinessAuditVO> list(MerchantBusinessAuditQuery query)
    {
        return auditMapper.selectList(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), query);
    }

    /**
     * 查询当前登录商户的一条业务审计详情。
     *
     * @param auditId 审计记录主键
     * @return 审计详情，不存在或不属于当前商户时返回 {@code null}
     */
    public MerchantBusinessAuditVO get(Long auditId)
    {
        return auditMapper.selectById(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), auditId);
    }

    /**
     * 查询当前商户日志列表对应的模块和操作人下拉选项。
     *
     * @return 与当前商户数据范围一致的审计筛选项
     */
    public MerchantBusinessAuditOptionsVO options()
    {
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        List<String> businessModules = auditMapper.selectBusinessModules(tenantId, memberId);
        List<String> operatorNames = auditMapper.selectOperatorNames(tenantId, memberId);
        MerchantBusinessAuditOptionsVO options = new MerchantBusinessAuditOptionsVO();
        options.setBusinessModules(businessModules);
        options.setOperatorNames(operatorNames);
        return options;
    }

    /**
     * 从当前商户登录上下文构造并写入业务审计记录。
     *
     * @param module 业务模块，例如“商品管理”或“订单管理”
     * @param operationType 业务动作编码，例如 CREATE、UPDATE、DELETE、CHANGE_PASSWORD
     * @param targetType 操作对象类型
     * @param targetId 操作对象主键或业务编码
     * @param targetName 操作对象名称
     * @param description 面向用户的业务描述
     * @param before 变更前对象，创建时可为空
     * @param after 变更后对象，删除时可为空
     */
    public void recordCurrent(String module, String operationType, String targetType, String targetId,
            String targetName, String description, Object before, Object after)
    {
        MerchantBusinessAuditCommand command = new MerchantBusinessAuditCommand();
        command.setTenantId(MerchantContextUtils.getTenantId());
        command.setMemberId(MerchantContextUtils.getMemberId());
        command.setOperatorUserId(SecurityUtils.getUserId());
        command.setOperatorName(SecurityUtils.getUsername());
        command.setBusinessModule(module);
        command.setOperationType(operationType);
        command.setTargetType(targetType);
        command.setTargetId(targetId);
        command.setTargetName(targetName);
        command.setDescription(description);
        command.setBeforeContent(snapshot(before));
        command.setAfterContent(snapshot(after));
        command.setIpAddress(IpUtils.getIpAddr());
        command.setStatus(0);
        record(command);
    }

    /**
     * 写入内部服务提交的业务审计命令，并统一补齐默认值和长度限制。
     *
     * @param command 内部业务审计命令
     * @return 受影响行数
     */
    public int record(MerchantBusinessAuditCommand command)
    {
        if (command == null || command.getTenantId() == null || command.getMemberId() == null)
        {
            throw new IllegalArgumentException("商户业务审计归属不能为空");
        }
        MerchantBusinessAuditRecord record = new MerchantBusinessAuditRecord();
        record.setTenantId(command.getTenantId());
        record.setMemberId(command.getMemberId());
        record.setOperatorUserId(command.getOperatorUserId());
        record.setOperatorName(limit(command.getOperatorName(), 64));
        record.setBusinessModule(limit(command.getBusinessModule(), 64));
        record.setOperationType(limit(command.getOperationType(), 32));
        record.setTargetType(limit(command.getTargetType(), 64));
        record.setTargetId(limit(command.getTargetId(), 128));
        record.setTargetName(limit(command.getTargetName(), 255));
        record.setDescription(limit(command.getDescription(), 1000));
        record.setBeforeContent(normalizeSnapshot(command.getBeforeContent()));
        record.setAfterContent(normalizeSnapshot(command.getAfterContent()));
        record.setIpAddress(limit(command.getIpAddress(), 64));
        record.setStatus(command.getStatus() == null ? 0 : command.getStatus());
        record.setErrorMessage(limit(command.getErrorMessage(), 2000));
        record.setTraceId(limit(command.getTraceId(), 64));
        record.setIsDelete(0);
        int rows = auditMapper.insert(record);
        log.info("商户业务审计落库完成：租户={}, 商户={}, 操作人={}, 模块={}, 操作={}, 对象={}, 影响行数={}",
                record.getTenantId(), record.getMemberId(), record.getOperatorUserId(),
                record.getBusinessModule(), record.getOperationType(), record.getTargetId(), rows);
        return rows;
    }

    /**
     * 将业务对象序列化成脱敏后的 JSON 快照。
     *
     * @param value 待记录对象
     * @return 脱敏 JSON，空对象返回空字符串
     */
    private String snapshot(Object value)
    {
        if (value == null)
        {
            return "";
        }
        try
        {
            return fitJson(JSON.toJSONString(value, new PropertyPreExcludeFilter()
                    .addExcludes(Arrays.copyOf(SENSITIVE_PROPERTIES, SENSITIVE_PROPERTIES.length))));
        }
        catch (RuntimeException exception)
        {
            log.warn("商户业务审计快照序列化失败: {}", exception.getMessage());
            return "";
        }
    }

    /**
     * 校验内部调用方传入的快照，并确保写入 JSON 字段的内容始终可解析。
     *
     * @param value 原始快照
     * @return 合法且不超过字段限制的 JSON 文本
     */
    private String normalizeSnapshot(String value)
    {
        if (StringUtils.isEmpty(value))
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

    /**
     * 对超长快照生成合法摘要，避免按字符截断后留下不完整 JSON。
     *
     * @param json 已序列化的 JSON 文本
     * @return 原始 JSON 或合法的截断摘要 JSON
     */
    private String fitJson(String json)
    {
        if (StringUtils.isEmpty(json) || json.length() <= CONTENT_MAX_LENGTH)
        {
            return StringUtils.defaultString(json);
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
