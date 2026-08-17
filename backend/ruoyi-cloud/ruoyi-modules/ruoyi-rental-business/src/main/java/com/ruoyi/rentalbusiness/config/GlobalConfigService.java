package com.ruoyi.rentalbusiness.config;

import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.system.api.RemoteGlobalConfigService;

/**
 * 商户业务服务统一全局配置读取服务。
 *
 * <p>配置值优先读取与平台端共用的 Redis 缓存，缓存未命中时通过平台内部 Feign 接口读取，
 * 并按七天有效期回填缓存。平台端更新配置会覆盖同一缓存键，因此不需要重启商户业务服务。</p>
 */
@Service
public class GlobalConfigService
{
    private final RemoteGlobalConfigService remoteGlobalConfigService;
    private final RedisService redisService;

    /**
     * 注入远程平台配置契约和 Redis 服务。
     *
     * @param remoteGlobalConfigService 平台全局配置内部契约
     * @param redisService Redis 服务
     */
    public GlobalConfigService(RemoteGlobalConfigService remoteGlobalConfigService, RedisService redisService)
    {
        this.remoteGlobalConfigService = remoteGlobalConfigService;
        this.redisService = redisService;
    }

    /**
     * 读取必填配置，缺失时返回空字符串。
     *
     * @param configKey 配置键
     * @return 配置值或空字符串
     */
    public String get(String configKey)
    {
        return get(configKey, StringUtils.EMPTY);
    }

    /**
     * 读取全局配置并使用指定默认值兜底。
     *
     * @param configKey 配置键
     * @param defaultValue 配置不存在或平台暂不可用时的默认值
     * @return 配置值
     */
    public String get(String configKey, String defaultValue)
    {
        if (!StringUtils.isNotEmpty(configKey))
        {
            return defaultValue;
        }
        String cacheKey = CacheConstants.GLOBAL_CONFIG_CACHE_KEY + configKey;
        Object cachedObject = redisService.getCacheObject(cacheKey);
        String cachedValue = cachedObject == null ? null : String.valueOf(cachedObject);
        if (cachedValue != null)
        {
            return cachedValue;
        }

        R<String> response = remoteGlobalConfigService.getValue(configKey, SecurityConstants.INNER);
        String value = response != null && R.isSuccess(response) && response.getData() != null
                ? response.getData() : defaultValue;
        long cacheTtlDays = GlobalConfigConstants.CACHE_TTL_DAYS.equals(configKey)
                ? CacheConstants.GLOBAL_CONFIG_CACHE_TTL_DAYS : getConfiguredCacheTtlDays();
        redisService.setCacheObject(cacheKey, value, cacheTtlDays, TimeUnit.DAYS);
        return value;
    }

    /**
     * 读取平台配置的缓存有效期。
     *
     * <p>优先使用共享 Redis 中的缓存期限；缓存未命中时通过同一个内部配置契约读取。
     * 读取缓存期限本身时由调用方使用七天默认值，避免递归请求。</p>
     *
     * @return 配置缓存有效期，单位为天
     */
    private long getConfiguredCacheTtlDays()
    {
        Object cachedObject = redisService.getCacheObject(CacheConstants.GLOBAL_CONFIG_CACHE_KEY
                + GlobalConfigConstants.CACHE_TTL_DAYS);
        String configuredValue = cachedObject == null ? null : String.valueOf(cachedObject);
        if (!StringUtils.isNotEmpty(configuredValue))
        {
            R<String> response = remoteGlobalConfigService.getValue(GlobalConfigConstants.CACHE_TTL_DAYS,
                    SecurityConstants.INNER);
            configuredValue = response != null && R.isSuccess(response) ? response.getData() : null;
        }
        try
        {
            long cacheTtlDays = Long.parseLong(configuredValue);
            return cacheTtlDays > 0 ? cacheTtlDays : CacheConstants.GLOBAL_CONFIG_CACHE_TTL_DAYS;
        }
        catch (Exception exception)
        {
            return CacheConstants.GLOBAL_CONFIG_CACHE_TTL_DAYS;
        }
    }
}
