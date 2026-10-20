package com.ruoyi.common.aliyun.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.ruoyi.common.aliyun.sms.internal.DefaultAliyunSmsService;

/** 根据运行时平台配置创建阿里云短信客户端。 */
public final class AliyunSmsServiceFactory
{
    private AliyunSmsServiceFactory()
    {
    }

    /**
     * 创建使用平台系统配置的短信发送器。
     *
     * @param endpoint 阿里云短信服务接入点
     * @param accessKeyId AccessKey ID
     * @param accessKeySecret AccessKey Secret
     * @return 短信发送器
     */
    public static AliyunSmsService create(String endpoint, String accessKeyId, String accessKeySecret)
    {
        if (isBlank(endpoint) || endpoint.contains("://") || endpoint.contains("/")
                || isBlank(accessKeyId) || isBlank(accessKeySecret))
        {
            throw new IllegalArgumentException("阿里云短信客户端配置不完整");
        }

        Config config = new Config()
                .setAccessKeyId(accessKeyId.trim())
                .setAccessKeySecret(accessKeySecret.trim());
        config.endpoint = endpoint.trim();
        try
        {
            return new DefaultAliyunSmsService(new Client(config));
        }
        catch (Exception exception)
        {
            throw new AliyunSmsException("阿里云短信客户端初始化失败", exception);
        }
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}
