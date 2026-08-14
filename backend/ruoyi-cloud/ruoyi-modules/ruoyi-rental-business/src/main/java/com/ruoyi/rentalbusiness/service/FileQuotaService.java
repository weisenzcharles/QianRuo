package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.mapper.RentalWorkflowMapper;
import com.ruoyi.rentalbusiness.model.FileQuotaVO;

/**
 * 商户文件配额与审计服务。
 *
 * <p>配额账户使用行锁和条件更新，所有导入文件先完成额度校验再写审计流水；导出不占用永久额度，
 * 但同样记录操作类型、文件名、业务资源和操作人，保证文件相关行为可追踪。</p>
 */
@Service
public class FileQuotaService
{
    private static final long DEFAULT_QUOTA_BYTES = 1024L * 1024L * 1024L;

    private final RentalWorkflowMapper mapper;
    private final MerchantBusinessAuditRecorder auditRecorder;

    /**
     * 注入文件配额 Mapper 和商户业务审计记录器。
     *
     * @param mapper 文件配额数据访问对象
     * @param auditRecorder 商户业务审计记录器
     */
    @Autowired
    public FileQuotaService(RentalWorkflowMapper mapper, MerchantBusinessAuditRecorder auditRecorder)
    {
        this.mapper = mapper;
        this.auditRecorder = auditRecorder;
    }

    /**
     * 保留旧构造方法，便于已有测试和离线工具使用文件配额服务。
     *
     * @param mapper 文件配额数据访问对象
     */
    public FileQuotaService(RentalWorkflowMapper mapper)
    {
        this(mapper, null);
    }

    /** 查询当前商户文件配额，首次访问时创建默认账户。 */
    @Transactional(rollbackFor = Exception.class)
    public FileQuotaVO getQuota()
    {
        Scope scope = scope();
        Map<String, Object> row = mapper.selectQuotaForUpdate(scope.tenantId, scope.memberId);
        if (row == null)
        {
            mapper.insertQuota(scope.tenantId, scope.memberId, DEFAULT_QUOTA_BYTES);
            row = mapper.selectQuotaForUpdate(scope.tenantId, scope.memberId);
        }
        if (row == null)
        {
            throw new ServiceException("文件配额账户创建失败");
        }
        FileQuotaVO result = new FileQuotaVO();
        result.quotaId = longValue(row, "quotaId");
        result.quotaBytes = longValue(row, "quotaBytes");
        result.usedBytes = longValue(row, "usedBytes");
        result.availableBytes = Math.max(0L, result.quotaBytes - result.usedBytes);
        return result;
    }

    /**
     * 记录一次文件操作，并在上传或导入时原子增加已使用配额。
     *
     * <p>文件配额流水和商户业务审计必须在同一事务内完成；审计写入失败时抛出异常，
     * 由调用方回滚配额变更，避免出现文件行为已经发生但商户日志缺失。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void record(String fileName, String fileUrl, long fileSize, String changeType,
            String refType, String refId)
    {
        if (fileSize < 0)
        {
            throw new ServiceException("文件大小不能为负数");
        }
        Scope scope = scope();
        getQuota();
        if (fileSize > 0 && mapper.increaseQuota(scope.tenantId, scope.memberId, fileSize) != 1)
        {
            throw new ServiceException("文件配额不足，当前文件不能保存");
        }
        mapper.insertQuotaLedger(scope.tenantId, scope.memberId, trim(fileName), trim(fileUrl), fileSize,
                required(changeType), trim(refType), trim(refId), operator());
        String operationType = "import".equalsIgnoreCase(changeType) ? "IMPORT"
                : "export".equalsIgnoreCase(changeType) ? "EXPORT" : "UPDATE";
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("fileName", trim(fileName));
        snapshot.put("fileUrl", trim(fileUrl));
        snapshot.put("fileSize", fileSize);
        snapshot.put("changeType", changeType);
        auditRecorder(operationType, refType, refId, fileName, "文件" + changeType + "：" + fileName,
                snapshot);
    }

    /** 查询当前商户文件配额审计流水。 */
    public List<Map<String, Object>> listLedger()
    {
        Scope scope = scope();
        List<Map<String, Object>> rows = mapper.selectQuotaLedger(scope.tenantId, scope.memberId);
        return rows == null ? Collections.<Map<String, Object>>emptyList() : new ArrayList<Map<String, Object>>(rows);
    }

    /** 获取当前商户范围。 */
    private Scope scope()
    {
        return new Scope(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
    }

    /** 获取操作人。 */
    private String operator()
    {
        return StringUtils.defaultIfEmpty(SecurityUtils.getUsername(), "system");
    }

    /** 校验操作类型。 */
    private String required(String value)
    {
        if (StringUtils.isBlank(value))
        {
            throw new ServiceException("文件操作类型不能为空");
        }
        return value.trim();
    }

    /** 去除可选字符串空白。 */
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    /** 从数据库行读取长整型。 */
    private Long longValue(Map<String, Object> row, String key)
    {
        Object value = row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    /**
     * 记录文件业务审计。业务审计采用严格模式，异常必须向上抛出以回滚文件配额主事务。
     *
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 业务对象标识
     * @param targetName 文件名称
     * @param description 业务描述
     * @param after 变更后摘要
     */
    private void auditRecorder(String operationType, String targetType, String targetId, String targetName,
            String description, Object after)
    {
        if (auditRecorder != null)
        {
            auditRecorder.record("文件管理", operationType, StringUtils.defaultIfEmpty(targetType, "FILE"),
                    targetId, targetName, description, null, after);
        }
    }

    /** 商户数据范围。 */
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
