package com.ruoyi.common.aliyun.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云短信连接配置。
 */
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties
{
    private boolean enabled;
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint = "dysmsapi.aliyuncs.com";

    public void validate()
    {
        if (isBlank(accessKeyId) || isBlank(accessKeySecret) || isBlank(endpoint)
                || endpoint.contains("://") || endpoint.contains("/"))
        {
            throw new IllegalStateException("aliyun.sms credentials and hostname endpoint are required when SMS is enabled");
        }
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getAccessKeyId()
    {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId)
    {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret()
    {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret)
    {
        this.accessKeySecret = accessKeySecret;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }
}
