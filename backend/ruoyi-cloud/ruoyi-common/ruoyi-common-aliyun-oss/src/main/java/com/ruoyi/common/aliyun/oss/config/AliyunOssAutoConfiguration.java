package com.ruoyi.common.aliyun.oss.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ruoyi.common.aliyun.oss.AliyunOssService;
import com.ruoyi.common.aliyun.oss.internal.DefaultAliyunOssService;

/**
 * 仅在显式启用时创建 OSS 客户端，避免未配置凭证的服务误连接云存储。
 */
@AutoConfiguration
@EnableConfigurationProperties(AliyunOssProperties.class)
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true")
public class AliyunOssAutoConfiguration
{
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(OSS.class)
    public OSS aliyunOssClient(AliyunOssProperties properties)
    {
        properties.validate();
        return new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKeyId(),
                properties.getAccessKeySecret());
    }

    @Bean
    @ConditionalOnMissingBean(AliyunOssService.class)
    public AliyunOssService aliyunOssService(OSS aliyunOssClient, AliyunOssProperties properties)
    {
        return new DefaultAliyunOssService(aliyunOssClient, properties);
    }
}
