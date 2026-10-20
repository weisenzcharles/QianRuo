package com.ruoyi.system.mapper.rental;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformBackupRecordQuery;
import com.ruoyi.system.model.rental.PlatformBackupRecordVO;
import com.ruoyi.system.model.rental.PlatformCapacityLedgerVO;
import com.ruoyi.system.model.rental.PlatformCapacityQuery;

/** 平台运维备份与容量流水数据访问组件。 */
public interface PlatformOperationsLedgerMapper
{
    List<PlatformBackupRecordVO> selectBackupRecords(@Param("query") PlatformBackupRecordQuery query);

    PlatformBackupRecordVO selectBackupByKey(@Param("backupKey") String backupKey);

    int insertBackupRecord(@Param("backupNo") String backupNo, @Param("backupType") Integer backupType,
            @Param("backupStatus") Integer backupStatus, @Param("backupKey") String backupKey,
            @Param("fileName") String fileName, @Param("fileSizeBytes") Long fileSizeBytes,
            @Param("startedTime") String startedTime, @Param("completedTime") String completedTime,
            @Param("retentionUntil") String retentionUntil, @Param("failureReason") String failureReason,
            @Param("operator") Long operator);

    PlatformBackupRecordVO selectLatestSuccessfulBackup();

    Long countBackupsByStatus(@Param("backupStatus") Integer backupStatus);

    List<PlatformCapacityLedgerVO> selectCapacityLedgers(@Param("query") PlatformCapacityQuery query);

    PlatformCapacityLedgerVO selectLatestCapacity(@Param("scopeType") Integer scopeType,
            @Param("scopeKey") String scopeKey);

    Map<String, Object> selectCapacitySummary();

    /** 查询当前平台数据库在 information_schema 中统计出的逻辑占用字节数。 */
    Long selectCurrentDatabaseUsedBytes();

    int insertCapacityLedger(@Param("scopeType") Integer scopeType, @Param("scopeKey") String scopeKey,
            @Param("scopeName") String scopeName, @Param("capacityBytes") Long capacityBytes,
            @Param("usedBytes") Long usedBytes, @Param("availableBytes") Long availableBytes,
            @Param("changedBytes") Long changedBytes, @Param("changeType") Integer changeType,
            @Param("snapshotSource") String snapshotSource, @Param("measuredTime") String measuredTime,
            @Param("operator") Long operator);
}
