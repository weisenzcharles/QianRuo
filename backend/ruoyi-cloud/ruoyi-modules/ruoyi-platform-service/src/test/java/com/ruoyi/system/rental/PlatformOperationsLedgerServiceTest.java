package com.ruoyi.system.rental;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.mapper.rental.PlatformOperationsLedgerMapper;
import com.ruoyi.system.model.rental.PlatformBackupRecordCommand;
import com.ruoyi.system.model.rental.PlatformBackupRecordQuery;
import com.ruoyi.system.model.rental.PlatformBackupRecordVO;
import com.ruoyi.system.model.rental.PlatformCapacityLedgerVO;
import com.ruoyi.system.model.rental.PlatformCapacityQuery;
import com.ruoyi.system.model.rental.PlatformCapacitySnapshotCommand;
import com.ruoyi.system.service.rental.PlatformOperationsLedgerService;

/** 平台运维台账枚举展示、容量变化和输入校验测试。 */
public class PlatformOperationsLedgerServiceTest
{
    @Test
    public void listDecoratesBackupAndCapacityLabels()
    {
        StubMapper mapper = new StubMapper();
        PlatformBackupRecordVO backup = new PlatformBackupRecordVO();
        backup.setBackupType(Integer.valueOf(1));
        backup.setBackupStatus(Integer.valueOf(1));
        mapper.backups = Collections.singletonList(backup);
        PlatformCapacityLedgerVO capacity = new PlatformCapacityLedgerVO();
        capacity.setScopeType(Integer.valueOf(2));
        capacity.setChangeType(Integer.valueOf(3));
        mapper.capacity = Collections.singletonList(capacity);

        PlatformOperationsLedgerService service = new PlatformOperationsLedgerService(mapper);

        Assert.assertEquals("数据库每日备份", service.listBackups(new PlatformBackupRecordQuery()).get(0).getBackupTypeName());
        Assert.assertEquals("成功", service.listBackups(new PlatformBackupRecordQuery()).get(0).getBackupStatusName());
        Assert.assertEquals("对象存储", service.listCapacity(new PlatformCapacityQuery()).get(0).getScopeTypeName());
        Assert.assertEquals("容量减少", service.listCapacity(new PlatformCapacityQuery()).get(0).getChangeTypeName());
    }

    @Test
    public void recordCapacity_calculatesIncreaseFromPreviousSnapshot()
    {
        StubMapper mapper = new StubMapper();
        PlatformCapacityLedgerVO previous = new PlatformCapacityLedgerVO();
        previous.setUsedBytes(Long.valueOf(800L));
        mapper.previous = previous;
        PlatformCapacitySnapshotCommand command = new PlatformCapacitySnapshotCommand();
        command.setScopeType(Integer.valueOf(1));
        command.setScopeKey("ry-rental-db");
        command.setScopeName("租赁数据库");
        command.setCapacityBytes(Long.valueOf(1000L));
        command.setUsedBytes(Long.valueOf(900L));
        command.setSnapshotSource("测试监控");
        command.setMeasuredTime("2026-09-28 10:00:00");

        new PlatformOperationsLedgerService(mapper).recordCapacity(command);

        Assert.assertEquals(Long.valueOf(100L), mapper.changedBytes);
        Assert.assertEquals(Integer.valueOf(2), mapper.changeType);
        Assert.assertEquals(Long.valueOf(100L), mapper.availableBytes);
    }

    @Test(expected = RuntimeException.class)
    public void registerBackup_failedWithoutReason_rejected()
    {
        PlatformBackupRecordCommand command = new PlatformBackupRecordCommand();
        command.setBackupType(Integer.valueOf(1));
        command.setBackupStatus(Integer.valueOf(2));
        command.setBackupKey("backups/test.sql.gz");
        new PlatformOperationsLedgerService(new StubMapper()).registerBackup(command);
    }

    private static class StubMapper implements PlatformOperationsLedgerMapper
    {
        private List<PlatformBackupRecordVO> backups = Collections.emptyList();
        private List<PlatformCapacityLedgerVO> capacity = Collections.emptyList();
        private PlatformCapacityLedgerVO previous;
        private Long changedBytes;
        private Long availableBytes;
        private Integer changeType;

        @Override public List<PlatformBackupRecordVO> selectBackupRecords(PlatformBackupRecordQuery query) { return backups; }
        @Override public PlatformBackupRecordVO selectBackupByKey(String backupKey) { return null; }
        @Override public int insertBackupRecord(String backupNo, Integer backupType, Integer backupStatus, String backupKey,
                String fileName, Long fileSizeBytes, String startedTime, String completedTime, String retentionUntil,
                String failureReason, Long operator) { return 1; }
        @Override public PlatformBackupRecordVO selectLatestSuccessfulBackup() { return null; }
        @Override public Long countBackupsByStatus(Integer backupStatus) { return Long.valueOf(0L); }
        @Override public List<PlatformCapacityLedgerVO> selectCapacityLedgers(PlatformCapacityQuery query) { return capacity; }
        @Override public PlatformCapacityLedgerVO selectLatestCapacity(Integer scopeType, String scopeKey) { return previous; }
        @Override public Map<String, Object> selectCapacitySummary() { return new HashMap<String, Object>(); }
        @Override public Long selectCurrentDatabaseUsedBytes() { return Long.valueOf(0L); }
        @Override public int insertCapacityLedger(Integer scopeType, String scopeKey, String scopeName, Long capacityBytes,
                Long usedBytes, Long availableBytes, Long changedBytes, Integer changeType, String snapshotSource,
                String measuredTime, Long operator)
        {
            this.changedBytes = changedBytes;
            this.availableBytes = availableBytes;
            this.changeType = changeType;
            return 1;
        }
    }
}
