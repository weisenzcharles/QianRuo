package com.ruoyi.file.config;

import java.util.Map;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteGlobalConfigService;

/** 从平台系统配置读取 OSS 参数，避免文件服务单独维护阿里云凭据。 */
@Component
public class PlatformAliyunOssConfigProvider
{
    private static final long CACHE_MILLIS = 60_000L;

    private final RemoteGlobalConfigService globalConfigService;
    private volatile AliyunOssConfig cachedConfig;
    private volatile long cachedAt;

    public PlatformAliyunOssConfigProvider(RemoteGlobalConfigService globalConfigService)
    {
        this.globalConfigService = globalConfigService;
    }

    /**
     * 读取当前 OSS 配置。短缓存避免每次上传都发起多次内部请求，系统配置修改最多延迟一分钟生效。
     *
     * @return OSS 配置
     */
    public AliyunOssConfig getConfig()
    {
        long now = System.currentTimeMillis();
        AliyunOssConfig current = cachedConfig;
        if (current != null && now - cachedAt < CACHE_MILLIS)
        {
            return current;
        }
        synchronized (this)
        {
            now = System.currentTimeMillis();
            if (cachedConfig == null || now - cachedAt >= CACHE_MILLIS)
            {
                cachedConfig = loadConfig();
                cachedAt = now;
            }
            return cachedConfig;
        }
    }

    private AliyunOssConfig loadConfig()
    {
        Map<String, String> values = Map.of(
                GlobalConfigConstants.ALIYUN_OSS_ENABLED, read(GlobalConfigConstants.ALIYUN_OSS_ENABLED, "false"),
                GlobalConfigConstants.ALIYUN_OSS_ENDPOINT, read(GlobalConfigConstants.ALIYUN_OSS_ENDPOINT, ""),
                GlobalConfigConstants.ALIYUN_ACCESS_KEY_ID, read(GlobalConfigConstants.ALIYUN_ACCESS_KEY_ID, ""),
                GlobalConfigConstants.ALIYUN_ACCESS_KEY_SECRET,
                read(GlobalConfigConstants.ALIYUN_ACCESS_KEY_SECRET, ""),
                GlobalConfigConstants.ALIYUN_OSS_BUCKET_NAME,
                read(GlobalConfigConstants.ALIYUN_OSS_BUCKET_NAME, ""),
                GlobalConfigConstants.ALIYUN_OSS_PUBLIC_BASE_URL,
                read(GlobalConfigConstants.ALIYUN_OSS_PUBLIC_BASE_URL, ""),
                GlobalConfigConstants.ALIYUN_OSS_OBJECT_PREFIX,
                read(GlobalConfigConstants.ALIYUN_OSS_OBJECT_PREFIX, "qianruo"));
        return new AliyunOssConfig(
                Boolean.parseBoolean(values.get(GlobalConfigConstants.ALIYUN_OSS_ENABLED)),
                values.get(GlobalConfigConstants.ALIYUN_OSS_ENDPOINT),
                values.get(GlobalConfigConstants.ALIYUN_ACCESS_KEY_ID),
                values.get(GlobalConfigConstants.ALIYUN_ACCESS_KEY_SECRET),
                values.get(GlobalConfigConstants.ALIYUN_OSS_BUCKET_NAME),
                values.get(GlobalConfigConstants.ALIYUN_OSS_PUBLIC_BASE_URL),
                values.get(GlobalConfigConstants.ALIYUN_OSS_OBJECT_PREFIX));
    }

    private String read(String configKey, String defaultValue)
    {
        R<String> result = globalConfigService.getValue(configKey, SecurityConstants.INNER);
        if (result == null || result.getCode() != R.SUCCESS)
        {
            return defaultValue;
        }
        return result.getData() == null ? defaultValue : result.getData().trim();
    }
}
