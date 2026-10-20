package com.ruoyi.file.config;

import java.io.InputStream;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ruoyi.common.aliyun.oss.AliyunOssException;
import com.ruoyi.common.aliyun.oss.AliyunOssService;
import com.ruoyi.common.aliyun.oss.AliyunOssUsageSnapshot;
import com.ruoyi.common.aliyun.oss.config.AliyunOssProperties;
import com.ruoyi.common.aliyun.oss.internal.DefaultAliyunOssService;

/** 使用平台系统配置动态创建 OSS 客户端的文件服务适配器。 */
public class PlatformConfiguredAliyunOssService implements AliyunOssService
{
    private static final long CLIENT_REFRESH_MILLIS = 60_000L;

    private final PlatformAliyunOssConfigProvider configProvider;
    private volatile ClientHolder clientHolder;

    public PlatformConfiguredAliyunOssService(PlatformAliyunOssConfigProvider configProvider)
    {
        this.configProvider = configProvider;
    }

    @Override
    public String upload(String objectKey, InputStream content, long contentLength, String contentType)
    {
        return getClient().service().upload(objectKey, content, contentLength, contentType);
    }

    @Override
    public void deleteByUrl(String fileUrl)
    {
        getClient().service().deleteByUrl(fileUrl);
    }

    @Override
    public AliyunOssUsageSnapshot usage()
    {
        return getClient().service().usage();
    }

    /** 释放当前 OSS 客户端。 */
    public synchronized void close()
    {
        if (clientHolder != null)
        {
            clientHolder.client().shutdown();
            clientHolder = null;
        }
    }

    private ClientHolder getClient()
    {
        ClientHolder current = clientHolder;
        long now = System.currentTimeMillis();
        if (current != null && now - current.createdAt() < CLIENT_REFRESH_MILLIS)
        {
            return current;
        }
        synchronized (this)
        {
            current = clientHolder;
            now = System.currentTimeMillis();
            if (current == null || now - current.createdAt() >= CLIENT_REFRESH_MILLIS)
            {
                ClientHolder replacement = createClient(configProvider.getConfig());
                clientHolder = replacement;
                if (current != null)
                {
                    current.client().shutdown();
                }
                return replacement;
            }
            return current;
        }
    }

    private ClientHolder createClient(AliyunOssConfig config)
    {
        if (!config.enabled())
        {
            throw new AliyunOssException("阿里云 OSS 尚未在平台系统配置中启用");
        }
        AliyunOssProperties properties = new AliyunOssProperties();
        properties.setEnabled(true);
        properties.setEndpoint(config.endpoint());
        properties.setAccessKeyId(config.accessKeyId());
        properties.setAccessKeySecret(config.accessKeySecret());
        properties.setBucketName(config.bucketName());
        properties.setPublicBaseUrl(config.publicBaseUrl());
        properties.validate();
        OSS client = new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKeyId(),
                properties.getAccessKeySecret());
        return new ClientHolder(client, new DefaultAliyunOssService(client, properties), System.currentTimeMillis());
    }

    private record ClientHolder(OSS client, AliyunOssService service, long createdAt)
    {
    }
}
