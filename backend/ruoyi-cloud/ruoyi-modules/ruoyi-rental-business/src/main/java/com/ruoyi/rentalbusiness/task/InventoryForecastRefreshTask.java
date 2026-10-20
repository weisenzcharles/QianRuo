package com.ruoyi.rentalbusiness.task;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.service.MerchantInventoryForecastService;

/** 统一调度入口调用的库存未来预测刷新任务。 */
@Component("inventoryForecastRefreshTask")
public class InventoryForecastRefreshTask
{
    private static final Logger log = LoggerFactory.getLogger(InventoryForecastRefreshTask.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String LOCK_KEY = "rental-business:task:inventory-forecast:refresh";
    private static final long LOCK_MINUTES = 30L;

    private final MerchantInventoryForecastService forecastService;
    private final RedisService redisService;

    public InventoryForecastRefreshTask(MerchantInventoryForecastService forecastService, RedisService redisService)
    {
        this.forecastService = forecastService;
        this.redisService = redisService;
    }

    /** 刷新所有商户未来三十天库存预测。 */
    public int refreshNextThirtyDays()
    {
        return refresh(LocalDate.now(BUSINESS_ZONE));
    }

    /** 按指定基准日刷新预测，供调度入口和测试复用。 */
    public int refresh(LocalDate baseDate)
    {
        String lockToken = UUID.randomUUID().toString();
        if (!tryAcquireLock(lockToken))
        {
            log.info("库存预测刷新任务已由其他实例执行，跳过本次任务");
            return 0;
        }
        try
        {
            int count = forecastService.refreshAll(baseDate);
            log.info("库存预测刷新任务完成，写入快照数={}", count);
            return count;
        }
        catch (RuntimeException exception)
        {
            log.error("库存预测刷新任务执行失败", exception);
            throw exception;
        }
        finally
        {
            releaseLock(lockToken);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean tryAcquireLock(String lockToken)
    {
        RedisTemplate redisTemplate = redisService.redisTemplate;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, lockToken, LOCK_MINUTES,
                TimeUnit.MINUTES);
        return Boolean.TRUE.equals(acquired);
    }

    private void releaseLock(String lockToken)
    {
        Object currentToken = redisService.getCacheObject(LOCK_KEY);
        if (lockToken.equals(currentToken == null ? null : String.valueOf(currentToken)))
        {
            redisService.deleteObject(LOCK_KEY);
        }
    }
}
