package com.ruoyi.rentalbusiness.task;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.service.MerchantInventoryReservationService;

/** 统一调度入口调用的外部库存预占过期释放任务。 */
@Component("externalInventoryReservationExpiryTask")
public class ExternalInventoryReservationExpiryTask
{
    private static final Logger log = LoggerFactory.getLogger(ExternalInventoryReservationExpiryTask.class);
    private static final String LOCK_KEY = "rental-business:task:external-inventory-reservation:expire";
    private static final long LOCK_MINUTES = 10L;

    private final MerchantInventoryReservationService reservationService;
    private final RedisService redisService;

    public ExternalInventoryReservationExpiryTask(MerchantInventoryReservationService reservationService,
            RedisService redisService)
    {
        this.reservationService = reservationService;
        this.redisService = redisService;
    }

    /** 释放已到期的外部库存预占，避免第三方超时请求长期占用库存。 */
    public int expire()
    {
        String lockToken = UUID.randomUUID().toString();
        if (!tryAcquireLock(lockToken))
        {
            log.info("外部库存预占过期任务已由其他实例执行，跳过本次任务");
            return 0;
        }
        try
        {
            int count = reservationService.expireExternalReservations();
            log.info("外部库存预占过期任务完成，释放预占数={}", count);
            return count;
        }
        catch (RuntimeException exception)
        {
            log.error("外部库存预占过期任务执行失败", exception);
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
