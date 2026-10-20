package com.ruoyi.system.api.model;

/** 文件服务返回给平台治理服务的对象存储使用量，不包含任何访问凭据。 */
public class PlatformObjectStorageUsage
{
    private boolean enabled;
    private long storageSizeBytes;
    private long objectCount;
    private String bucketName;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public long getStorageSizeBytes()
    {
        return storageSizeBytes;
    }

    public void setStorageSizeBytes(long storageSizeBytes)
    {
        this.storageSizeBytes = storageSizeBytes;
    }

    public long getObjectCount()
    {
        return objectCount;
    }

    public void setObjectCount(long objectCount)
    {
        this.objectCount = objectCount;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }
}
