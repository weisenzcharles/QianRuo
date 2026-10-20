package com.ruoyi.common.aliyun.oss;

/**
 * 阿里云 OSS Bucket 的只读使用量快照。
 *
 * <p>OSS SDK 可以返回已使用字节数和对象数量，但总容量由云厂商配额或监控配置提供，
 * 因此这里不混入“总容量”字段，避免把使用量误当成配额。</p>
 */
public record AliyunOssUsageSnapshot(long storageSizeBytes, long objectCount)
{
}
