package com.ruoyi.common.aliyun.oss.internal;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import org.junit.Test;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.BucketStat;
import com.ruoyi.common.aliyun.oss.AliyunOssUsageSnapshot;
import com.ruoyi.common.aliyun.oss.config.AliyunOssProperties;

import static org.junit.Assert.assertEquals;

/**
 * OSS 对象键与删除地址边界测试。
 */
public class DefaultAliyunOssServiceTest
{
    @Test(expected = IllegalArgumentException.class)
    public void upload_parentTraversalKey_rejectedBeforeSdkCall()
    {
        DefaultAliyunOssService service = new DefaultAliyunOssService(null, configuredProperties());
        service.upload("../private.txt", new ByteArrayInputStream(new byte[] { 1 }), 1, "text/plain");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteByUrl_foreignDomain_rejectedBeforeSdkCall()
    {
        DefaultAliyunOssService service = new DefaultAliyunOssService(null, configuredProperties());
        service.deleteByUrl("https://attacker.example/2026/secret.txt");
    }

    @Test
    public void publicBaseUrl_withoutOverride_usesBucketVirtualHost()
    {
        assertEquals("https://qianruo.oss-cn-hangzhou.aliyuncs.com",
                configuredProperties().resolvePublicBaseUrl());
    }

    @Test
    public void usage_readsBucketStorageAndObjectCount()
    {
        OSS client = (OSS) Proxy.newProxyInstance(OSS.class.getClassLoader(), new Class<?>[] { OSS.class },
                (proxy, method, args) -> "getBucketStat".equals(method.getName())
                        ? new BucketStat(123L, 4L, 0L) : null);

        AliyunOssUsageSnapshot snapshot = new DefaultAliyunOssService(client, configuredProperties()).usage();

        assertEquals(123L, snapshot.storageSizeBytes());
        assertEquals(4L, snapshot.objectCount());
    }

    private AliyunOssProperties configuredProperties()
    {
        AliyunOssProperties properties = new AliyunOssProperties();
        properties.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        properties.setAccessKeyId("test-id");
        properties.setAccessKeySecret("test-secret");
        properties.setBucketName("qianruo");
        return properties;
    }
}
