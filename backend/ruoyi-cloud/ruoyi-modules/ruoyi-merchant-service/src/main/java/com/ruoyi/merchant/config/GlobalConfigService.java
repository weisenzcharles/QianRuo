package com.ruoyi.merchant.config;

import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.system.api.RemoteGlobalConfigService;

/** 商户服务读取平台系统配置的缓存服务。 */
@Service
public class GlobalConfigService
{
    private final RemoteGlobalConfigService remoteGlobalConfigService;
    private final RedisService redisService;

    public GlobalConfigService(RemoteGlobalConfigService remoteGlobalConfigService, RedisService redisService)
    {
        this.remoteGlobalConfigService = remoteGlobalConfigService;
        this.redisService = redisService;
    }

    /** 读取系统配置，命中共享 Redis 时不调用平台服务。 */
    public String get(String configKey)
    {
        return get(configKey, StringUtils.EMPTY);
    }

    /** 读取系统配置并使用默认值兜底。 */
    public String get(String configKey, String defaultValue)
    {
        if (!StringUtils.isNotEmpty(configKey))
        {
            return defaultValue;
        }
        String cacheKey = CacheConstants.GLOBAL_CONFIG_CACHE_KEY + configKey;
        Object cached = redisService.getCacheObject(cacheKey);
        if (cached != null)
        {
            return String.valueOf(cached);
        }
        R<String> response = remoteGlobalConfigService.getValue(configKey, SecurityConstants.INNER);
        String value = response != null && R.isSuccess(response) && response.getData() != null
                ? response.getData() : defaultValue;
        redisService.setCacheObject(cacheKey, value, CacheConstants.GLOBAL_CONFIG_CACHE_TTL_DAYS, TimeUnit.DAYS);
        return value;
    }

    /** 清晰表达支付宝配置读取由当前服务托管，保留统一入口便于后续配置刷新扩展。 */
    public boolean isEnabled(String configKey)
    {
        return Boolean.parseBoolean(get(configKey, "false"));
    }
}
