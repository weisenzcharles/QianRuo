package com.ruoyi.system.service.rental;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.enums.PlatformBackupStatus;
import com.ruoyi.system.enums.PlatformBackupType;
import com.ruoyi.system.enums.PlatformCapacityChangeType;
import com.ruoyi.system.enums.PlatformCapacityScopeType;
import com.ruoyi.system.mapper.rental.PlatformOperationsLedgerMapper;
import com.ruoyi.system.model.rental.PlatformBackupRecordCommand;
import com.ruoyi.system.model.rental.PlatformBackupRecordQuery;
import com.ruoyi.system.model.rental.PlatformBackupRecordVO;
import com.ruoyi.system.model.rental.PlatformCapacityLedgerVO;
import com.ruoyi.system.model.rental.PlatformCapacityQuery;
import com.ruoyi.system.model.rental.PlatformCapacitySnapshotCommand;
import com.ruoyi.system.model.rental.PlatformOperationsLedgerSummaryVO;

/**
 * 平台运维台账服务。
 *
 * <p>备份执行由部署脚本或运维任务负责，本服务只登记执行结果和容量快照，保证平台能追溯保留期限、失败原因
 * 以及容量变化，不把“页面显示成功”当成真实备份完成。</p>
 */
@Service
public class PlatformOperationsLedgerService
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_BACKUP_KEY_LENGTH = 255;
    private static final int MAX_FILE_NAME_LENGTH = 255;
    private static final int MAX_SCOPE_KEY_LENGTH = 100;
    private static final int MAX_SCOPE_NAME_LENGTH = 200;
    private static final int MAX_SOURCE_LENGTH = 100;
    private static final int MAX_FAILURE_REASON_LENGTH = 1000;

    private final PlatformOperationsLedgerMapper mapper;

    public PlatformOperationsLedgerService(PlatformOperationsLedgerMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询平台备份记录并补全中文枚举名称。 */
    public List<PlatformBackupRecordVO> listBackups(PlatformBackupRecordQuery query)
    {
        List<PlatformBackupRecordVO> rows = mapper.selectBackupRecords(query == null ? new PlatformBackupRecordQuery() : query);
        if (rows == null || rows.isEmpty())
            return Collections.emptyList();
        for (PlatformBackupRecordVO row : rows)
            decorateBackup(row);
        return rows;
    }

    /** 查询容量流水并补全中文枚举名称。 */
    public List<PlatformCapacityLedgerVO> listCapacity(PlatformCapacityQuery query)
    {
        List<PlatformCapacityLedgerVO> rows = mapper.selectCapacityLedgers(query == null ? new PlatformCapacityQuery() : query);
        if (rows == null || rows.isEmpty())
            return Collections.emptyList();
        for (PlatformCapacityLedgerVO row : rows)
            decorateCapacity(row);
        return rows;
    }

    /** 查询备份和容量的运营摘要。 */
    public PlatformOperationsLedgerSummaryVO summary()
    {
        PlatformOperationsLedgerSummaryVO result = new PlatformOperationsLedgerSummaryVO();
        result.setLatestSuccessfulBackup(mapper.selectLatestSuccessfulBackup());
        if (result.getLatestSuccessfulBackup() != null)
            decorateBackup(result.getLatestSuccessfulBackup());
        result.setBackupSuccessCount(valueOrZero(mapper.countBackupsByStatus(PlatformBackupStatus.SUCCESS.getCode())));
        result.setBackupFailureCount(valueOrZero(mapper.countBackupsByStatus(PlatformBackupStatus.FAILED.getCode())));
        Map<String, Object> capacity = mapper.selectCapacitySummary();
        result.setLatestUsedBytes(number(capacity, "latest_used_bytes"));
        result.setLatestAvailableBytes(number(capacity, "latest_available_bytes"));
        result.setLatestMeasuredTime(text(capacity, "latest_measured_time"));
        return result;
    }

    /** 登记一次备份执行结果，备份对象键重复时拒绝重复记录。 */
    public void registerBackup(PlatformBackupRecordCommand command)
    {
        if (command == null)
            throw new ServiceException("备份记录不能为空");
        PlatformBackupType type = requiredBackupType(command.getBackupType());
        PlatformBackupStatus status = requiredBackupStatus(command.getBackupStatus());
        String backupKey = requiredText(command.getBackupKey(), "备份对象键", MAX_BACKUP_KEY_LENGTH);
        if (mapper.selectBackupByKey(backupKey) != null)
            throw new ServiceException("该备份对象已经登记，不能重复提交");
        Long fileSizeBytes = nonNegative(command.getFileSizeBytes(), "备份文件大小");
        String startedTime = normalizeDateTime(command.getStartedTime(), LocalDateTime.now());
        String completedTime = optionalDateTime(command.getCompletedTime(), "备份完成时间");
        String retentionUntil = optionalDateTime(command.getRetentionUntil(), "备份保留截止时间");
        if (status == PlatformBackupStatus.SUCCESS && completedTime == null)
            completedTime = startedTime;
        if (status == PlatformBackupStatus.FAILED && requiredText(command.getFailureReason(), "失败原因", MAX_FAILURE_REASON_LENGTH).isEmpty())
            throw new ServiceException("备份失败时必须填写失败原因");
        if (retentionUntil == null)
            retentionUntil = normalizeDateTime(null, LocalDateTime.parse(startedTime, DATE_TIME_FORMATTER)
                    .plusDays(type == PlatformBackupType.MONTHLY_LOG_PACKAGE ? 31L : 7L));
        String fileName = optionalText(command.getFileName(), MAX_FILE_NAME_LENGTH);
        String failureReason = optionalText(command.getFailureReason(), MAX_FAILURE_REASON_LENGTH);
        mapper.insertBackupRecord(generateBackupNo(), type.getCode(), status.getCode(), backupKey, fileName, fileSizeBytes,
                startedTime, completedTime, retentionUntil, failureReason, SecurityUtils.getUserId());
    }

    /** 登记容量快照，并根据上一次快照计算已用容量变化方向。 */
    public void recordCapacity(PlatformCapacitySnapshotCommand command)
    {
        if (command == null)
            throw new ServiceException("容量快照不能为空");
        PlatformCapacityScopeType scopeType = requiredScopeType(command.getScopeType());
        String scopeKey = requiredText(command.getScopeKey(), "容量范围业务键", MAX_SCOPE_KEY_LENGTH);
        String scopeName = requiredText(command.getScopeName(), "容量范围名称", MAX_SCOPE_NAME_LENGTH);
        Long capacityBytes = nonNegative(command.getCapacityBytes(), "总容量");
        Long usedBytes = nonNegative(command.getUsedBytes(), "已使用容量");
        if (usedBytes.longValue() > capacityBytes.longValue())
            throw new ServiceException("已使用容量不能大于总容量");
        long expectedAvailable = capacityBytes.longValue() - usedBytes.longValue();
        Long availableBytes = command.getAvailableBytes() == null ? Long.valueOf(expectedAvailable)
                : nonNegative(command.getAvailableBytes(), "可用容量");
        if (availableBytes.longValue() != expectedAvailable)
            throw new ServiceException("可用容量必须等于总容量减去已使用容量");
        String snapshotSource = requiredText(command.getSnapshotSource(), "快照来源", MAX_SOURCE_LENGTH);
        String measuredTime = normalizeDateTime(command.getMeasuredTime(), LocalDateTime.now());
        PlatformCapacityLedgerVO previous = mapper.selectLatestCapacity(scopeType.getCode(), scopeKey);
        long changedBytes = previous == null ? 0L : usedBytes.longValue() - valueOrZero(previous.getUsedBytes());
        int changeType = previous == null ? PlatformCapacityChangeType.INITIAL.getCode()
                : changedBytes > 0L ? PlatformCapacityChangeType.INCREASE.getCode()
                : changedBytes < 0L ? PlatformCapacityChangeType.DECREASE.getCode()
                : PlatformCapacityChangeType.UNCHANGED.getCode();
        mapper.insertCapacityLedger(scopeType.getCode(), scopeKey, scopeName, capacityBytes, usedBytes, availableBytes,
                Long.valueOf(changedBytes), changeType, snapshotSource, measuredTime, SecurityUtils.getUserId());
    }

    private void decorateBackup(PlatformBackupRecordVO row)
    {
        PlatformBackupType type = PlatformBackupType.fromCode(row.getBackupType());
        PlatformBackupStatus status = PlatformBackupStatus.fromCode(row.getBackupStatus());
        row.setBackupTypeName(type == null ? "未知类型" : type.getLabel());
        row.setBackupStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private void decorateCapacity(PlatformCapacityLedgerVO row)
    {
        PlatformCapacityScopeType scopeType = PlatformCapacityScopeType.fromCode(row.getScopeType());
        PlatformCapacityChangeType changeType = PlatformCapacityChangeType.fromCode(row.getChangeType());
        row.setScopeTypeName(scopeType == null ? "未知范围" : scopeType.getLabel());
        row.setChangeTypeName(changeType == null ? "未知变化" : changeType.getLabel());
    }

    private PlatformBackupType requiredBackupType(Integer code)
    {
        PlatformBackupType value = PlatformBackupType.fromCode(code);
        if (value == null)
            throw new ServiceException("备份类型不合法");
        return value;
    }

    private PlatformBackupStatus requiredBackupStatus(Integer code)
    {
        PlatformBackupStatus value = PlatformBackupStatus.fromCode(code);
        if (value == null)
            throw new ServiceException("备份状态不合法");
        return value;
    }

    private PlatformCapacityScopeType requiredScopeType(Integer code)
    {
        PlatformCapacityScopeType value = PlatformCapacityScopeType.fromCode(code);
        if (value == null)
            throw new ServiceException("容量范围不合法");
        return value;
    }

    private String requiredText(String value, String label, int maxLength)
    {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty())
            throw new ServiceException(label + "不能为空");
        if (normalized.length() > maxLength)
            throw new ServiceException(label + "长度不能超过" + maxLength + "个字符");
        return normalized;
    }

    private String optionalText(String value, int maxLength)
    {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > maxLength)
            return normalized.substring(0, maxLength);
        return normalized.isEmpty() ? null : normalized;
    }

    private Long nonNegative(Long value, String label)
    {
        if (value == null || value.longValue() < 0L)
            throw new ServiceException(label + "不能小于0");
        return value;
    }

    private String optionalDateTime(String value, String label)
    {
        if (value == null || value.trim().isEmpty())
            return null;
        return normalizeDateTime(value, null, label);
    }

    private String normalizeDateTime(String value, LocalDateTime defaultValue)
    {
        return normalizeDateTime(value, defaultValue, "时间");
    }

    private String normalizeDateTime(String value, LocalDateTime defaultValue, String label)
    {
        if (value == null || value.trim().isEmpty())
            return (defaultValue == null ? LocalDateTime.now() : defaultValue).format(DATE_TIME_FORMATTER);
        String normalized = value.trim().replace('T', ' ');
        try
        {
            return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException(label + "格式不正确，应为yyyy-MM-dd HH:mm:ss");
        }
    }

    private String generateBackupNo()
    {
        return "BK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private long valueOrZero(Long value)
    {
        return value == null ? 0L : value.longValue();
    }

    private String text(Map<String, Object> values, String key)
    {
        if (values == null || values.get(key) == null)
            return null;
        return String.valueOf(values.get(key));
    }

    private Long number(Map<String, Object> values, String key)
    {
        if (values == null || values.get(key) == null)
            return Long.valueOf(0L);
        Object value = values.get(key);
        return value instanceof Number ? Long.valueOf(((Number) value).longValue()) : Long.valueOf(0L);
    }
}
