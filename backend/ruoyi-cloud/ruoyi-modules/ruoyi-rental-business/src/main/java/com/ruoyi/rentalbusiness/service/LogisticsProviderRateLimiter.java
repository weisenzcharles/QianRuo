package com.ruoyi.rentalbusiness.service;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.integration.express.ExpressProvider;

/**
 * 物流供应商全站秒级限流器。
 *
 * <p>顺丰和快递100共用各自的 Redis 秒桶，避免多实例部署时每个实例分别突破供应商
 * 的请求上限。限流结果由调用方转换为“查询中”，不把供应商拒绝伪装成业务失败。</p>
 */
@Service
public class LogisticsProviderRateLimiter
{
    static final int MAX_REQUESTS_PER_SECOND = 10;

    private static final Logger log = LoggerFactory.getLogger(LogisticsProviderRateLimiter.class);

    private static final String KEY_PREFIX = "rental:logistics:provider-rate:";

    private final RedisService redisService;

    public LogisticsProviderRateLimiter(RedisService redisService)
    {
        this.redisService = redisService;
    }

    /**
     * 尝试为一次供应商查询分配当前秒的额度。
     *
     * @param provider 供应商
     * @return 当前秒有额度或 Redis 暂不可用时返回 true；超过额度返回 false
     */
    public boolean tryAcquire(ExpressProvider provider)
    {
        if (provider == null || redisService == null || redisService.redisTemplate == null)
        {
            return true;
        }
        String key = KEY_PREFIX + provider.name().toLowerCase(Locale.ROOT) + ":"
                + (System.currentTimeMillis() / 1000L);
        try
        {
            Long count = redisService.redisTemplate.opsForValue().increment(key, 1L);
            if (count != null && count.longValue() == 1L)
            {
                redisService.expire(key, 2L, TimeUnit.SECONDS);
            }
            return isWithinLimit(count);
        }
        catch (RuntimeException exception)
        {
            // 限流基础设施异常时允许查询继续，避免 Redis 故障放大成全站物流不可用。
            log.warn("物流供应商限流计数失败，provider={}, message={}", provider, exception.getMessage());
            return true;
        }
    }

    static boolean isWithinLimit(Long count)
    {
        return count == null || count.longValue() <= MAX_REQUESTS_PER_SECOND;
    }
}
