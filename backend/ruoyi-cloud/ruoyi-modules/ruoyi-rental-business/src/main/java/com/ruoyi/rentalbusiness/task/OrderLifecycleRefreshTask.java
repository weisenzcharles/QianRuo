package com.ruoyi.rentalbusiness.task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.service.MerchantOrderLifecycleService;

/** 统一调度入口调用的订单履约状态刷新任务。 */
@Component("orderLifecycleRefreshTask")
public class OrderLifecycleRefreshTask
{
    private static final Logger log = LoggerFactory.getLogger(OrderLifecycleRefreshTask.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String LOCK_KEY = "rental-business:task:order-lifecycle:refresh";
    private static final long LOCK_MINUTES = 5L;

    private final MerchantOrderLifecycleService lifecycleService;
    private final RedisService redisService;

    public OrderLifecycleRefreshTask(MerchantOrderLifecycleService lifecycleService, RedisService redisService)
    {
        this.lifecycleService = lifecycleService;
        this.redisService = redisService;
    }

    /** 刷新达到履约时间条件的全部订单。 */
    public int refresh()
    {
        String lockToken = UUID.randomUUID().toString();
        if (!tryAcquireLock(lockToken))
        {
            log.info("订单履约状态刷新任务已由其他实例执行，跳过本次任务");
            return 0;
        }
        try
        {
            int changed = lifecycleService.refresh(LocalDateTime.now(BUSINESS_ZONE));
            log.info("订单履约状态刷新任务完成，推进订单数={}", changed);
            return changed;
        }
        catch (RuntimeException exception)
        {
            log.error("订单履约状态刷新任务执行失败", exception);
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
