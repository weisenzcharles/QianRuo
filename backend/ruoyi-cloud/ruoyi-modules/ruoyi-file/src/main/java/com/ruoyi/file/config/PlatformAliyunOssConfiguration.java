package com.ruoyi.file.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.aliyun.oss.AliyunOssService;
import com.ruoyi.common.aliyun.oss.config.AliyunOssAutoConfiguration;

/** 文件服务统一使用平台系统配置管理的 OSS 客户端。 */
@Configuration
@AutoConfigureBefore(AliyunOssAutoConfiguration.class)
@ConditionalOnProperty(prefix = "file", name = "storage-provider", havingValue = "aliyun-oss", matchIfMissing = true)
public class PlatformAliyunOssConfiguration
{
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(AliyunOssService.class)
    public AliyunOssService platformConfiguredAliyunOssService(
            PlatformAliyunOssConfigProvider configProvider)
    {
        return new PlatformConfiguredAliyunOssService(configProvider);
    }
}
