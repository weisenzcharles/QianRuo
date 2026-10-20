package com.ruoyi.system.service.rental;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteFileService;
import com.ruoyi.system.api.model.PlatformObjectStorageUsage;
import com.ruoyi.system.enums.PlatformAlertEventType;
import com.ruoyi.system.enums.PlatformCapacityScopeType;
import com.ruoyi.system.mapper.rental.PlatformAlertEventMapper;
import com.ruoyi.system.mapper.rental.PlatformOperationsLedgerMapper;
import com.ruoyi.system.model.rental.PlatformCapacitySnapshotCommand;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 平台文件系统容量采集服务。
 *
 * <p>采集平台服务实际可见、且由系统配置指定的文件系统路径；对象存储只使用文件服务返回的
 * OSS Bucket 实际已用量，并要求平台系统配置云厂商确认的总容量，避免跨存储介质误报。</p>
 */
@Service
public class PlatformCapacityCollectionService
{
    static final String PATH_CONFIG_KEY = "platform.operations.capacity.path";
    static final String WARNING_PERCENT_CONFIG_KEY = "platform.operations.capacity.min-available-percent";
    static final String OBJECT_STORAGE_CAPACITY_CONFIG_KEY = "platform.operations.object-storage.capacity-bytes";
    static final String DATABASE_CAPACITY_CONFIG_KEY = "platform.operations.database.capacity-bytes";
    static final String DEFAULT_PATH = ".";
    static final int DEFAULT_WARNING_PERCENT = 10;
    private static final int MAX_SCOPE_KEY_LENGTH = 100;

    private final ISysConfigService configService;
    private final PlatformOperationsLedgerService ledgerService;
    private final PlatformAlertEventMapper alertMapper;
    private final RemoteFileService remoteFileService;
    private final PlatformOperationsLedgerMapper operationsMapper;

    public PlatformCapacityCollectionService(ISysConfigService configService,
            PlatformOperationsLedgerService ledgerService, PlatformAlertEventMapper alertMapper,
            RemoteFileService remoteFileService, PlatformOperationsLedgerMapper operationsMapper)
    {
        this.configService = configService;
        this.ledgerService = ledgerService;
        this.alertMapper = alertMapper;
        this.remoteFileService = remoteFileService;
        this.operationsMapper = operationsMapper;
    }

    /** 采集配置路径容量并返回成功采集的范围数量。 */
    public int collect()
    {
        Path path;
        String scopeKey;
        try
        {
            path = configuredPath();
            scopeKey = scopeKey(path);
        }
        catch (RuntimeException exception)
        {
            recordFailure("configured-path", exception);
            throw new ServiceException("平台文件存储容量采集失败，请检查容量路径配置");
        }
        int collectedCount;
        try
        {
            collectedCount = collect(path, scopeKey);
        }
        catch (IOException exception)
        {
            recordFailure(scopeKey, exception);
            throw new ServiceException("平台文件存储容量采集失败，请检查容量路径配置");
        }
        catch (RuntimeException exception)
        {
            recordFailure(scopeKey, exception);
            throw exception;
        }
        return collectedCount + collectObjectStorage() + collectDatabase();
    }

    /**
     * 采集 OSS 的实际已用量。总容量必须由平台系统配置录入云厂商确认的配额，未配置时跳过，
     * 避免将对象数量或已用容量误当成总容量。
     */
    private int collectObjectStorage()
    {
        if (remoteFileService == null)
        {
            return 0;
        }
        String scopeKey = "aliyun-oss";
        try
        {
            R<PlatformObjectStorageUsage> response = remoteFileService.usage(SecurityConstants.INNER);
            if (response == null || !R.isSuccess(response))
            {
                throw new ServiceException(response == null ? "文件服务未返回对象存储使用量"
                        : response.getMsg());
            }
            PlatformObjectStorageUsage usage = response.getData();
            if (usage == null || !usage.isEnabled())
            {
                alertMapper.resolveStorageAlert(objectStorageFailureAlertKey(scopeKey), SecurityUtils.getUserId(),
                        "对象存储未启用或已按文件服务配置关闭");
                return 0;
            }

            Long capacityBytes = objectStorageCapacityBytes();
            if (capacityBytes == null)
            {
                return 0;
            }
            long usedBytes = usage.getStorageSizeBytes();
            if (usedBytes < 0L || usedBytes > capacityBytes.longValue())
            {
                throw new ServiceException("对象存储已用容量超过配置的总容量");
            }

            PlatformCapacitySnapshotCommand command = new PlatformCapacitySnapshotCommand();
            command.setScopeType(Integer.valueOf(PlatformCapacityScopeType.OBJECT_STORAGE.getCode()));
            command.setScopeKey(scopeKey);
            command.setScopeName("阿里云 OSS 对象存储");
            command.setCapacityBytes(capacityBytes);
            command.setUsedBytes(Long.valueOf(usedBytes));
            command.setAvailableBytes(Long.valueOf(capacityBytes.longValue() - usedBytes));
            command.setSnapshotSource("aliyun-oss-bucket-stat");
            ledgerService.recordCapacity(command);

            int availablePercent = (int) Math.min(100d,
                    (capacityBytes.longValue() - usedBytes) * 100d / capacityBytes.longValue());
            String alertKey = objectStorageAlertKey(scopeKey);
            if (availablePercent <= warningPercent())
            {
                int alertLevel = availablePercent <= 5 ? 3 : 2;
                alertMapper.upsertStorageAlert(alertKey, Integer.valueOf(PlatformAlertEventType.STORAGE.getCode()),
                        Integer.valueOf(alertLevel), "对象存储空间不足",
                        "阿里云 OSS 可用空间仅剩 " + availablePercent + "%，请及时扩容或清理",
                        scopeKey, SecurityUtils.getUserId());
            }
            else
            {
                alertMapper.resolveStorageAlert(alertKey, SecurityUtils.getUserId(), "对象存储可用空间已恢复到安全阈值以上");
            }
            alertMapper.resolveStorageAlert(objectStorageFailureAlertKey(scopeKey), SecurityUtils.getUserId(),
                    "对象存储容量采集已恢复");
            return 1;
        }
        catch (RuntimeException exception)
        {
            recordObjectStorageFailure(scopeKey, exception);
            throw new ServiceException("对象存储容量采集失败，请检查 OSS 配置和文件服务");
        }
    }

    /**
     * 采集当前平台数据库在 information_schema 中的逻辑数据和索引占用量。总容量仍需由数据库
     * 云厂商配额或监控确认后配置，避免将逻辑占用误报成物理磁盘剩余空间。
     */
    private int collectDatabase()
    {
        if (operationsMapper == null)
        {
            return 0;
        }
        String scopeKey = "platform-database";
        try
        {
            Long capacityBytes = databaseCapacityBytes();
            if (capacityBytes == null)
            {
                return 0;
            }
            Long usedValue = operationsMapper.selectCurrentDatabaseUsedBytes();
            long usedBytes = usedValue == null ? 0L : usedValue.longValue();
            if (usedBytes < 0L || usedBytes > capacityBytes.longValue())
            {
                throw new ServiceException("数据库逻辑占用量超过配置的总容量");
            }

            PlatformCapacitySnapshotCommand command = new PlatformCapacitySnapshotCommand();
            command.setScopeType(Integer.valueOf(PlatformCapacityScopeType.DATABASE.getCode()));
            command.setScopeKey(scopeKey);
            command.setScopeName("平台业务数据库逻辑容量");
            command.setCapacityBytes(capacityBytes);
            command.setUsedBytes(Long.valueOf(usedBytes));
            command.setAvailableBytes(Long.valueOf(capacityBytes.longValue() - usedBytes));
            command.setSnapshotSource("mysql-information-schema");
            ledgerService.recordCapacity(command);

            int availablePercent = (int) Math.min(100d,
                    (capacityBytes.longValue() - usedBytes) * 100d / capacityBytes.longValue());
            String alertKey = databaseAlertKey(scopeKey);
            if (availablePercent <= warningPercent())
            {
                int alertLevel = availablePercent <= 5 ? 3 : 2;
                alertMapper.upsertStorageAlert(alertKey, Integer.valueOf(PlatformAlertEventType.STORAGE.getCode()),
                        Integer.valueOf(alertLevel), "数据库容量不足",
                        "平台业务数据库可用空间仅剩 " + availablePercent + "%，请及时扩容或清理",
                        scopeKey, SecurityUtils.getUserId());
            }
            else
            {
                alertMapper.resolveStorageAlert(alertKey, SecurityUtils.getUserId(), "数据库可用空间已恢复到安全阈值以上");
            }
            alertMapper.resolveStorageAlert(databaseFailureAlertKey(scopeKey), SecurityUtils.getUserId(),
                    "数据库容量采集已恢复");
            return 1;
        }
        catch (RuntimeException exception)
        {
            recordDatabaseFailure(scopeKey, exception);
            throw new ServiceException("数据库容量采集失败，请检查数据库权限和容量配置");
        }
    }

    /**
     * 采集指定路径容量，单元测试和运维诊断可直接复用该测量边界。
     *
     * @param path 平台服务可见的文件系统路径
     * @param scopeKey 写入容量台账的稳定业务键
     * @return 成功采集的容量范围数量
     * @throws IOException 文件系统无法返回合法容量时抛出
     */
    public int collect(Path path, String scopeKey) throws IOException
    {
        FileStore fileStore = Files.getFileStore(path);
        long capacityBytes = fileStore.getTotalSpace();
        long availableBytes = fileStore.getUsableSpace();
        if (capacityBytes <= 0L || availableBytes < 0L || availableBytes > capacityBytes)
            throw new IOException("文件系统容量返回值不合法");

        long usedBytes = capacityBytes - availableBytes;
        PlatformCapacitySnapshotCommand command = new PlatformCapacitySnapshotCommand();
        command.setScopeType(Integer.valueOf(PlatformCapacityScopeType.FILE_STORAGE.getCode()));
        command.setScopeKey(scopeKey);
        command.setScopeName("平台运行文件存储");
        command.setCapacityBytes(Long.valueOf(capacityBytes));
        command.setUsedBytes(Long.valueOf(usedBytes));
        command.setAvailableBytes(Long.valueOf(availableBytes));
        command.setSnapshotSource("platform-capacity-job");
        ledgerService.recordCapacity(command);

        int availablePercent = (int) Math.min(100d, availableBytes * 100d / (double) capacityBytes);
        String alertKey = alertKey(scopeKey);
        if (availablePercent <= warningPercent())
        {
            int alertLevel = availablePercent <= 5 ? 3 : 2;
            alertMapper.upsertStorageAlert(alertKey, Integer.valueOf(PlatformAlertEventType.STORAGE.getCode()),
                    Integer.valueOf(alertLevel), "平台文件存储空间不足",
                    "路径 " + scopeKey + " 可用空间仅剩 " + availablePercent + "%，请及时扩容或清理",
                    scopeKey, SecurityUtils.getUserId());
        }
        else
        {
            alertMapper.resolveStorageAlert(alertKey, SecurityUtils.getUserId(), "文件存储可用空间已恢复到安全阈值以上");
        }
        alertMapper.resolveStorageAlert(failureAlertKey(scopeKey), SecurityUtils.getUserId(), "容量采集已恢复");
        return 1;
    }

    private void recordFailure(String scopeKey, Throwable exception)
    {
        String detail = exception == null || exception.getMessage() == null
                ? "平台文件存储容量采集失败" : exception.getMessage();
        if (detail.length() > 1000)
            detail = detail.substring(0, 1000);
        alertMapper.upsertStorageAlert(failureAlertKey(scopeKey), Integer.valueOf(PlatformAlertEventType.SYSTEM.getCode()),
                Integer.valueOf(3), "平台容量采集失败", detail, scopeKey, SecurityUtils.getUserId());
    }

    private void recordObjectStorageFailure(String scopeKey, Throwable exception)
    {
        String detail = exception == null || exception.getMessage() == null
                ? "对象存储容量采集失败" : exception.getMessage();
        if (detail.length() > 1000)
            detail = detail.substring(0, 1000);
        alertMapper.upsertStorageAlert(objectStorageFailureAlertKey(scopeKey),
                Integer.valueOf(PlatformAlertEventType.SYSTEM.getCode()), Integer.valueOf(3),
                "对象存储容量采集失败", detail, scopeKey, SecurityUtils.getUserId());
    }

    private void recordDatabaseFailure(String scopeKey, Throwable exception)
    {
        String detail = exception == null || exception.getMessage() == null
                ? "数据库容量采集失败" : exception.getMessage();
        if (detail.length() > 1000)
            detail = detail.substring(0, 1000);
        alertMapper.upsertStorageAlert(databaseFailureAlertKey(scopeKey),
                Integer.valueOf(PlatformAlertEventType.SYSTEM.getCode()), Integer.valueOf(3),
                "数据库容量采集失败", detail, scopeKey, SecurityUtils.getUserId());
    }

    private Path configuredPath()
    {
        String configured = configService == null ? null : configService.selectConfigByKey(PATH_CONFIG_KEY);
        String value = configured == null || configured.trim().isEmpty() ? DEFAULT_PATH : configured.trim();
        return Paths.get(value).toAbsolutePath().normalize();
    }

    private int warningPercent()
    {
        String configured = configService == null ? null : configService.selectConfigByKey(WARNING_PERCENT_CONFIG_KEY);
        if (configured == null || configured.trim().isEmpty())
            return DEFAULT_WARNING_PERCENT;
        try
        {
            int value = Integer.parseInt(configured.trim());
            return value >= 0 && value <= 100 ? value : DEFAULT_WARNING_PERCENT;
        }
        catch (NumberFormatException exception)
        {
            return DEFAULT_WARNING_PERCENT;
        }
    }

    private Long objectStorageCapacityBytes()
    {
        String configured = configService == null ? null
                : configService.selectConfigByKey(OBJECT_STORAGE_CAPACITY_CONFIG_KEY);
        if (configured == null || configured.trim().isEmpty())
        {
            return null;
        }
        try
        {
            long value = Long.parseLong(configured.trim());
            if (value <= 0L)
            {
                throw new ServiceException("对象存储总容量必须大于0");
            }
            return Long.valueOf(value);
        }
        catch (NumberFormatException exception)
        {
            throw new ServiceException("对象存储总容量必须使用字节数整数配置");
        }
    }

    private Long databaseCapacityBytes()
    {
        String configured = configService == null ? null
                : configService.selectConfigByKey(DATABASE_CAPACITY_CONFIG_KEY);
        if (configured == null || configured.trim().isEmpty())
        {
            return null;
        }
        try
        {
            long value = Long.parseLong(configured.trim());
            if (value <= 0L)
            {
                throw new ServiceException("数据库总容量必须大于0");
            }
            return Long.valueOf(value);
        }
        catch (NumberFormatException exception)
        {
            throw new ServiceException("数据库总容量必须使用字节数整数配置");
        }
    }

    private String scopeKey(Path path)
    {
        String value = path.toString();
        return value.length() <= MAX_SCOPE_KEY_LENGTH ? value : value.substring(0, MAX_SCOPE_KEY_LENGTH);
    }

    private String alertKey(String scopeKey)
    {
        return "STORAGE_CAPACITY:" + scopeKey;
    }

    private String failureAlertKey(String scopeKey)
    {
        return "STORAGE_CAPACITY_FAILURE:" + scopeKey;
    }

    private String objectStorageAlertKey(String scopeKey)
    {
        return "OBJECT_STORAGE_CAPACITY:" + scopeKey;
    }

    private String objectStorageFailureAlertKey(String scopeKey)
    {
        return "OBJECT_STORAGE_CAPACITY_FAILURE:" + scopeKey;
    }

    private String databaseAlertKey(String scopeKey)
    {
        return "DATABASE_CAPACITY:" + scopeKey;
    }

    private String databaseFailureAlertKey(String scopeKey)
    {
        return "DATABASE_CAPACITY_FAILURE:" + scopeKey;
    }
}
