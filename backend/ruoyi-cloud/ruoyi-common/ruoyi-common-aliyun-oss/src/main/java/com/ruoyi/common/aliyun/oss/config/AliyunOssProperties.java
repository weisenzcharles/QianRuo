package com.ruoyi.common.aliyun.oss.config;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 连接与访问地址配置。
 */
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties
{
    private boolean enabled;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String publicBaseUrl;

    public void validate()
    {
        requireText(endpoint, "aliyun.oss.endpoint");
        requireText(accessKeyId, "aliyun.oss.access-key-id");
        requireText(accessKeySecret, "aliyun.oss.access-key-secret");
        requireText(bucketName, "aliyun.oss.bucket-name");
        URI endpointUri = URI.create(endpoint);
        if (!isHttpScheme(endpointUri.getScheme()) || endpointUri.getHost() == null
                || (endpointUri.getRawPath() != null && !endpointUri.getRawPath().isEmpty()
                        && !"/".equals(endpointUri.getRawPath())))
        {
            throw new IllegalStateException("aliyun.oss.endpoint must be an HTTP(S) OSS endpoint without a path");
        }
        if (!bucketName.matches("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$"))
        {
            throw new IllegalStateException("aliyun.oss.bucket-name must be a valid lowercase OSS bucket name");
        }
        if (publicBaseUrl != null && !publicBaseUrl.isBlank())
        {
            URI publicUri = URI.create(trimTrailingSlash(publicBaseUrl));
            if (!isHttpScheme(publicUri.getScheme()) || publicUri.getHost() == null
                    || publicUri.getRawUserInfo() != null || publicUri.getRawQuery() != null
                    || publicUri.getRawFragment() != null)
            {
                throw new IllegalStateException("aliyun.oss.public-base-url must be an HTTP(S) URL without query or fragment");
            }
        }
    }

    public String resolvePublicBaseUrl()
    {
        validate();
        if (publicBaseUrl != null && !publicBaseUrl.isBlank())
        {
            return trimTrailingSlash(publicBaseUrl);
        }

        URI endpointUri = URI.create(endpoint);
        String host = endpointUri.getHost();
        String bucketPrefix = bucketName + ".";
        if (!host.startsWith(bucketPrefix))
        {
            host = bucketPrefix + host;
        }
        String port = endpointUri.getPort() < 0 ? "" : ":" + endpointUri.getPort();
        return endpointUri.getScheme() + "://" + host + port;
    }

    private static boolean isHttpScheme(String scheme)
    {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static void requireText(String value, String propertyName)
    {
        if (value == null || value.isBlank())
        {
            throw new IllegalStateException(propertyName + " is required when Aliyun OSS is enabled");
        }
    }

    private static String trimTrailingSlash(String value)
    {
        String result = value.trim();
        while (result.endsWith("/"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
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

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getPublicBaseUrl()
    {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl)
    {
        this.publicBaseUrl = publicBaseUrl;
    }
}
