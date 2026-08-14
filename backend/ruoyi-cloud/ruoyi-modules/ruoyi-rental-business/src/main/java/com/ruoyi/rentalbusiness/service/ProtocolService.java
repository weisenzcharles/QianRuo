package com.ruoyi.rentalbusiness.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.mapper.RentalWorkflowMapper;
import com.ruoyi.rentalbusiness.model.ProtocolAcceptanceCommand;
import com.ruoyi.rentalbusiness.model.ProtocolCommand;
import com.ruoyi.rentalbusiness.model.ProtocolVO;

/**
 * 协议版本服务。
 *
 * <p>平台端负责创建、修改和发布不可逆版本；发布同一协议键的新版本时自动下线旧版本。
 * 商户端只读取当前有效版本并以商户用户为主体写确认记录，确认记录保存正文哈希，保证之后
 * 即使协议正文被误改也能追溯当时确认的实际内容。</p>
 */
@Service
public class ProtocolService
{
    private final RentalWorkflowMapper mapper;
    private final MerchantBusinessAuditRecorder auditRecorder;

    /**
     * 注入协议数据访问对象和商户业务审计记录器。
     *
     * @param mapper 协议数据访问对象
     * @param auditRecorder 商户业务审计记录器
     */
    @Autowired
    public ProtocolService(RentalWorkflowMapper mapper, MerchantBusinessAuditRecorder auditRecorder)
    {
        this.mapper = mapper;
        this.auditRecorder = auditRecorder;
    }

    /**
     * 保留旧构造方法，便于已有测试和离线工具构造协议服务。
     *
     * @param mapper 协议数据访问对象
     */
    public ProtocolService(RentalWorkflowMapper mapper)
    {
        this(mapper, null);
    }

    /** 查询平台协议版本列表。 */
    public List<ProtocolVO> listPlatform(String protocolKey)
    {
        assertPlatform();
        return mapProtocols(mapper.selectProtocols(trim(protocolKey), false), false);
    }

    /** 查询当前商户需要确认的已发布协议版本。 */
    public List<ProtocolVO> listMerchant(String protocolKey)
    {
        Scope scope = scope();
        List<ProtocolVO> result = mapProtocols(mapper.selectProtocols(trim(protocolKey), true), true);
        for (ProtocolVO item : result)
        {
            item.accepted = mapper.selectAcceptance(scope.tenantId, scope.memberId, SecurityUtils.getUserId(),
                    item.protocolId, item.versionNo) != null;
        }
        return result;
    }

    /** 新增协议草稿并自动生成该协议键下的下一个版本号。 */
    @Transactional(rollbackFor = Exception.class)
    public ProtocolVO add(ProtocolCommand command)
    {
        assertPlatform();
        validate(command);
        int versionNo = mapper.selectMaxProtocolVersion(command.protocolKey.trim()) + 1;
        mapper.insertProtocol(command.protocolKey.trim(), versionNo, command.protocolTitle.trim(),
                command.protocolContent, sha256(command.protocolContent), "draft", operator(), command.remark);
        ProtocolVO result = getPlatformByKeyVersion(command.protocolKey.trim(), versionNo);
        audit("CREATE", command.protocolKey, String.valueOf(result.protocolId), command.protocolTitle,
                "创建协议版本：" + command.protocolKey + " v" + versionNo, null, result);
        return result;
    }

    /** 修改协议草稿；已发布版本禁止覆盖正文。 */
    @Transactional(rollbackFor = Exception.class)
    public ProtocolVO update(ProtocolCommand command)
    {
        assertPlatform();
        validate(command);
        Map<String, Object> existing = mapper.selectProtocol(command.protocolId);
        if (existing == null)
        {
            throw new ServiceException("协议版本不存在");
        }
        if ("published".equals(stringValue(existing, "protocolStatus")))
        {
            throw new ServiceException("已发布协议版本不可修改，请新建版本");
        }
        mapper.updateProtocol(command.protocolId, command.protocolTitle.trim(), command.protocolContent,
                sha256(command.protocolContent), "draft", operator(), command.remark);
        ProtocolVO result = toView(mapper.selectProtocol(command.protocolId), false);
        audit("UPDATE", command.protocolKey, String.valueOf(command.protocolId), command.protocolTitle,
                "修改协议版本：" + command.protocolKey, toView(existing, false), result);
        return result;
    }

    /** 发布协议版本并下线同协议键下的其他版本。 */
    @Transactional(rollbackFor = Exception.class)
    public ProtocolVO publish(Long protocolId)
    {
        assertPlatform();
        Map<String, Object> existing = mapper.selectProtocol(protocolId);
        if (existing == null)
        {
            throw new ServiceException("协议版本不存在");
        }
        String key = required(stringValue(existing, "protocolKey"), "协议业务键不能为空");
        mapper.retireProtocols(key, operator());
        mapper.updateProtocol(protocolId, stringValue(existing, "protocolTitle"),
                stringValue(existing, "protocolContent"), stringValue(existing, "contentHash"),
                "published", operator(), null);
        ProtocolVO result = toView(mapper.selectProtocol(protocolId), false);
        audit("UPDATE", key, String.valueOf(protocolId), stringValue(existing, "protocolTitle"),
                "发布协议版本：" + key, toView(existing, false), result);
        return result;
    }

    /** 当前商户确认一个已发布协议版本。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean accept(ProtocolAcceptanceCommand command)
    {
        Scope scope = scope();
        Long protocolId = command == null ? null : command.protocolId;
        Map<String, Object> protocol = mapper.selectProtocol(protocolId);
        if (protocol == null || !"published".equals(stringValue(protocol, "protocolStatus")))
        {
            throw new ServiceException("协议版本不存在或尚未发布");
        }
        mapper.insertAcceptance(scope.tenantId, scope.memberId, SecurityUtils.getUserId(), protocolId,
                stringValue(protocol, "protocolKey"), number(protocol, "versionNo"),
                stringValue(protocol, "contentHash"), operator());
        audit("ACCEPT", stringValue(protocol, "protocolKey"), String.valueOf(protocolId),
                stringValue(protocol, "protocolTitle"), "确认协议版本：" + protocolId, null, command);
        return true;
    }

    /** 将协议数据库行转换为展示对象。 */
    private List<ProtocolVO> mapProtocols(List<Map<String, Object>> rows, boolean includeContent)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<ProtocolVO> result = new ArrayList<ProtocolVO>(rows.size());
        for (Map<String, Object> row : rows)
        {
            result.add(toView(row, includeContent));
        }
        return result;
    }

    /** 转换单条协议版本并按端口控制正文展示。 */
    private ProtocolVO toView(Map<String, Object> row, boolean includeContent)
    {
        ProtocolVO view = new ProtocolVO();
        view.protocolId = longValue(row, "protocolId");
        view.protocolKey = stringValue(row, "protocolKey");
        view.versionNo = number(row, "versionNo");
        view.protocolTitle = stringValue(row, "protocolTitle");
        view.protocolContent = includeContent ? stringValue(row, "protocolContent") : null;
        view.contentHash = stringValue(row, "contentHash");
        view.protocolStatus = stringValue(row, "protocolStatus");
        return view;
    }

    /** 按协议键和版本查询刚创建的版本。 */
    private ProtocolVO getPlatformByKeyVersion(String key, int versionNo)
    {
        for (Map<String, Object> row : mapper.selectProtocols(key, false))
        {
            if (number(row, "versionNo") == versionNo)
            {
                return toView(row, false);
            }
        }
        throw new ServiceException("协议版本保存失败");
    }

    /** 平台接口必须使用平台客户端类型。 */
    private void assertPlatform()
    {
        if (!ClientContextHolder.isPlatformClient())
            throw new ServiceException("当前接口仅允许平台端访问");
    }

    /** 获取当前商户上下文。 */
    private Scope scope()
    {
        return new Scope(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
    }

    /** 校验协议写入命令。 */
    private void validate(ProtocolCommand command)
    {
        if (command == null || StringUtils.isBlank(command.protocolKey)
                || StringUtils.isBlank(command.protocolTitle) || StringUtils.isBlank(command.protocolContent))
        {
            throw new ServiceException("协议键、标题和正文不能为空");
        }
    }

    /** 计算协议正文 SHA-256。 */
    private String sha256(String value)
    {
        try
        {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte item : bytes) result.append(String.format("%02x", item));
            return result.toString();
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("JDK 缺少 SHA-256 算法", exception);
        }
    }

    /** 获取操作人。 */
    private String operator()
    {
        return StringUtils.defaultIfEmpty(SecurityUtils.getUsername(), "system");
    }

    /**
     * 记录协议业务日志，平台调用会由通用记录器自动忽略，商户确认操作会正常入库。
     *
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 业务对象标识
     * @param targetName 业务对象名称
     * @param description 业务描述
     * @param before 变更前内容
     * @param after 变更后内容
     */
    private void audit(String operationType, String targetType, String targetId, String targetName,
            String description, Object before, Object after)
    {
        if (auditRecorder != null)
        {
            auditRecorder.record("协议管理", operationType, "PROTOCOL", targetId, targetName,
                    description, before, after);
        }
    }

    /** 校验文本并去除空白。 */
    private String required(String value, String message)
    {
        if (StringUtils.isBlank(value)) throw new ServiceException(message);
        return value.trim();
    }

    /** 去除可选查询条件空白。 */
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    /** 从数据库行读取文本。 */
    private String stringValue(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** 从数据库行读取长整型。 */
    private Long longValue(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    /** 从数据库行读取整型。 */
    private int number(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? 0 : ((Number) value).intValue();
    }

    /** 商户上下文。 */
    private static final class Scope
    {
        private final Long tenantId;
        private final Long memberId;

        private Scope(Long tenantId, Long memberId)
        {
            this.tenantId = tenantId;
            this.memberId = memberId;
        }
    }
}
