package com.ruoyi.system.rental;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteFileService;
import com.ruoyi.system.api.domain.SysFile;
import com.ruoyi.system.mapper.rental.PlatformAlertEventMapper;
import com.ruoyi.system.mapper.rental.PlatformOperationsLedgerMapper;
import com.ruoyi.system.model.rental.PlatformAlertEventQuery;
import com.ruoyi.system.model.rental.PlatformAlertEventVO;
import com.ruoyi.system.model.rental.PlatformBackupRecordQuery;
import com.ruoyi.system.model.rental.PlatformBackupRecordVO;
import com.ruoyi.system.model.rental.PlatformCapacityLedgerVO;
import com.ruoyi.system.model.rental.PlatformCapacityQuery;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.rental.PlatformCapacityCollectionService;
import com.ruoyi.system.service.rental.PlatformOperationsLedgerService;
import com.ruoyi.system.api.model.PlatformObjectStorageUsage;

/** 平台文件系统容量采集、阈值告警和 Mapper 契约测试。 */
public class PlatformCapacityCollectionServiceTest
{
    @Test
    public void collect_writesFileCapacityAndResolvesHealthyAlerts() throws Exception
    {
        StubConfig config = new StubConfig(".", "", "", "0");
        StubLedgerMapper ledgerMapper = new StubLedgerMapper();
        StubAlertMapper alertMapper = new StubAlertMapper();
        PlatformCapacityCollectionService service = new PlatformCapacityCollectionService(config,
                new PlatformOperationsLedgerService(ledgerMapper), alertMapper, null, ledgerMapper);

        Assert.assertEquals(1, service.collect(Paths.get("."), "/test-path"));
        Assert.assertEquals(Integer.valueOf(3), ledgerMapper.scopeType);
        Assert.assertTrue(ledgerMapper.capacityBytes > 0L);
        Assert.assertEquals(2, alertMapper.resolveCount);
        Assert.assertEquals("STORAGE_CAPACITY_FAILURE:/test-path", alertMapper.lastResolvedKey);
    }

    @Test
    public void collect_invalidPath_persistsSystemAlert() throws Exception
    {
        StubConfig config = new StubConfig("/path-that-does-not-exist");
        StubLedgerMapper ledgerMapper = new StubLedgerMapper();
        StubAlertMapper alertMapper = new StubAlertMapper();
        PlatformCapacityCollectionService service = new PlatformCapacityCollectionService(config,
                new PlatformOperationsLedgerService(ledgerMapper), alertMapper, null, ledgerMapper);

        try
        {
            service.collect();
            Assert.fail("missing path must fail");
        }
        catch (RuntimeException expected)
        {
            Assert.assertEquals("STORAGE_CAPACITY_FAILURE:/path-that-does-not-exist", alertMapper.lastAlertKey);
            Assert.assertEquals(Integer.valueOf(5), alertMapper.lastAlertType);
        }
    }

    @Test
    public void collect_configuredOssQuota_writesObjectStorageCapacity() throws Exception
    {
        StubConfig config = new StubConfig(".", "1000");
        StubLedgerMapper ledgerMapper = new StubLedgerMapper();
        StubAlertMapper alertMapper = new StubAlertMapper();
        PlatformObjectStorageUsage usage = new PlatformObjectStorageUsage();
        usage.setEnabled(true);
        usage.setStorageSizeBytes(123L);
        usage.setObjectCount(4L);
        RemoteFileService remoteFileService = new RemoteFileService()
        {
            @Override public R<SysFile> upload(MultipartFile file, String businessType) { return R.fail(); }
            @Override public R<Boolean> delete(String fileUrl) { return R.fail(); }
            @Override public R<PlatformObjectStorageUsage> usage(String source) { return R.ok(usage); }
        };
        PlatformCapacityCollectionService service = new PlatformCapacityCollectionService(config,
                new PlatformOperationsLedgerService(ledgerMapper), alertMapper, remoteFileService, ledgerMapper);

        Assert.assertEquals(2, service.collect());
        Assert.assertEquals(2, ledgerMapper.insertCount);
        Assert.assertEquals(Integer.valueOf(2), ledgerMapper.scopeType);
        Assert.assertEquals(Long.valueOf(123L), ledgerMapper.usedBytes);
        Assert.assertEquals(Long.valueOf(877L), ledgerMapper.availableBytes);
    }

    @Test
    public void collect_configuredDatabaseQuota_writesLogicalDatabaseCapacity() throws Exception
    {
        StubConfig config = new StubConfig(".", "", "1000");
        StubLedgerMapper ledgerMapper = new StubLedgerMapper();
        StubAlertMapper alertMapper = new StubAlertMapper();
        PlatformCapacityCollectionService service = new PlatformCapacityCollectionService(config,
                new PlatformOperationsLedgerService(ledgerMapper), alertMapper, null, ledgerMapper);

        Assert.assertEquals(2, service.collect());
        Assert.assertEquals(2, ledgerMapper.insertCount);
        Assert.assertEquals(Integer.valueOf(1), ledgerMapper.scopeType);
        Assert.assertEquals(Long.valueOf(0L), ledgerMapper.usedBytes);
        Assert.assertEquals(Long.valueOf(1000L), ledgerMapper.availableBytes);
    }

    private static class StubConfig implements ISysConfigService
    {
        private final String path;
        private final String objectStorageCapacity;
        private final String databaseCapacity;
        private final String warningPercent;

        private StubConfig()
        {
            this(".", "", "", "10");
        }

        private StubConfig(String path)
        {
            this(path, "", "", "10");
        }

        private StubConfig(String path, String objectStorageCapacity)
        {
            this(path, objectStorageCapacity, "", "10");
        }

        private StubConfig(String path, String objectStorageCapacity, String databaseCapacity)
        {
            this(path, objectStorageCapacity, databaseCapacity, "10");
        }

        private StubConfig(String path, String objectStorageCapacity, String databaseCapacity, String warningPercent)
        {
            this.path = path;
            this.objectStorageCapacity = objectStorageCapacity;
            this.databaseCapacity = databaseCapacity;
            this.warningPercent = warningPercent;
        }

        @Override public String selectConfigByKey(String configKey)
        {
            if (configKey.endsWith("percent"))
                return warningPercent;
            if (configKey.contains("object-storage"))
                return objectStorageCapacity;
            if (configKey.contains("database"))
                return databaseCapacity;
            return path;
        }
        @Override public com.ruoyi.system.domain.SysConfig selectConfigById(Long configId) { return null; }
        @Override public List<com.ruoyi.system.domain.SysConfig> selectConfigList(com.ruoyi.system.domain.SysConfig config) { return Collections.emptyList(); }
        @Override public int insertConfig(com.ruoyi.system.domain.SysConfig config) { return 0; }
        @Override public int updateConfig(com.ruoyi.system.domain.SysConfig config) { return 0; }
        @Override public void deleteConfigByIds(Long[] configIds) { }
        @Override public void loadingConfigCache() { }
        @Override public void clearConfigCache() { }
        @Override public void resetConfigCache() { }
        @Override public boolean checkConfigKeyUnique(com.ruoyi.system.domain.SysConfig config) { return false; }
    }

    private static class StubLedgerMapper implements PlatformOperationsLedgerMapper
    {
        private Integer scopeType;
        private int insertCount;
        private long capacityBytes;
        private Long usedBytes;
        private Long availableBytes;
        @Override public List<PlatformBackupRecordVO> selectBackupRecords(PlatformBackupRecordQuery query) { return Collections.emptyList(); }
        @Override public PlatformBackupRecordVO selectBackupByKey(String backupKey) { return null; }
        @Override public int insertBackupRecord(String backupNo, Integer backupType, Integer backupStatus, String backupKey,
                String fileName, Long fileSizeBytes, String startedTime, String completedTime, String retentionUntil,
                String failureReason, Long operator) { return 1; }
        @Override public PlatformBackupRecordVO selectLatestSuccessfulBackup() { return null; }
        @Override public Long countBackupsByStatus(Integer backupStatus) { return Long.valueOf(0L); }
        @Override public List<PlatformCapacityLedgerVO> selectCapacityLedgers(PlatformCapacityQuery query) { return Collections.emptyList(); }
        @Override public PlatformCapacityLedgerVO selectLatestCapacity(Integer scopeType, String scopeKey) { return null; }
        @Override public Map<String, Object> selectCapacitySummary() { return new HashMap<String, Object>(); }
        @Override public Long selectCurrentDatabaseUsedBytes() { return Long.valueOf(0L); }
        @Override public int insertCapacityLedger(Integer scopeType, String scopeKey, String scopeName, Long capacityBytes,
                Long usedBytes, Long availableBytes, Long changedBytes, Integer changeType, String snapshotSource,
                String measuredTime, Long operator)
        {
            this.scopeType = scopeType;
            this.capacityBytes = capacityBytes.longValue();
            this.usedBytes = usedBytes;
            this.availableBytes = availableBytes;
            this.insertCount++;
            return 1;
        }
    }

    private static class StubAlertMapper implements PlatformAlertEventMapper
    {
        private String lastAlertKey;
        private Integer lastAlertType;
        private String lastResolvedKey;
        private int resolveCount;
        @Override public List<PlatformAlertEventVO> selectList(PlatformAlertEventQuery query) { return Collections.emptyList(); }
        @Override public int countOpenEvents() { return 0; }
        @Override public List<Map<String, Object>> selectRecentFailedJobs() { return Collections.emptyList(); }
        @Override public int upsertScheduledTaskAlert(String alertEventKey, String title, String detail, String sourceKey,
                Long occurrenceCount, Long operator) { return 1; }
        @Override public int upsertStorageAlert(String alertEventKey, Integer alertType, Integer alertLevel, String title,
                String detail, String sourceKey, Long operator)
        {
            lastAlertKey = alertEventKey;
            lastAlertType = alertType;
            return 1;
        }
        @Override public int resolveStorageAlert(String alertEventKey, Long operator, String remark)
        {
            lastResolvedKey = alertEventKey;
            resolveCount++;
            return 1;
        }
        @Override public int upsertExternalInterfaceAlert(String alertEventKey, Integer alertLevel, String title,
                String detail, String sourceKey, Long operator) { return 1; }
        @Override public int resolveExternalInterfaceAlert(String alertEventKey, Long operator, String remark)
        { return 1; }
        @Override public int resolveMissingScheduledTaskAlerts(List<String> alertEventKeys, Long operator) { return 1; }
        @Override public PlatformAlertEventVO selectById(Long alertEventId) { return null; }
        @Override public int acknowledge(Long alertEventId, Long operator) { return 1; }
        @Override public int resolve(Long alertEventId, String remark, Long operator) { return 1; }
    }
}
