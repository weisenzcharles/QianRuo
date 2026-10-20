package com.ruoyi.file.config;

/** 平台系统配置中的 OSS 运行参数。 */
public record AliyunOssConfig(boolean enabled, String endpoint, String accessKeyId,
        String accessKeySecret, String bucketName, String publicBaseUrl, String objectPrefix)
{
}
