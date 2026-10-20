package com.ruoyi.rentalbusiness.task;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.service.MerchantExternalOrderCallbackService;

/** 统一调度入口调用的外部订单回调出站任务。 */
@Component("merchantExternalOrderCallbackTask")
public class MerchantExternalOrderCallbackTask
{
    private static final Logger log = LoggerFactory.getLogger(MerchantExternalOrderCallbackTask.class);
    private static final String LOCK_KEY = "rental-business:task:external-order-callback:dispatch";
    private static final long LOCK_MINUTES = 5L;

    private final MerchantExternalOrderCallbackService callbackService;
    private final RedisService redisService;

    public MerchantExternalOrderCallbackTask(MerchantExternalOrderCallbackService callbackService,
            RedisService redisService)
    {
        this.callbackService = callbackService;
        this.redisService = redisService;
    }

    /** 发送到期的订单回调事件，失败事件由出站表按退避时间再次调度。 */
    public int dispatch()
    {
        String lockToken = UUID.randomUUID().toString();
        if (!tryAcquireLock(lockToken))
        {
            log.info("外部订单回调任务已由其他实例执行，跳过本次任务");
            return 0;
        }
        try
        {
            int delivered = callbackService.dispatchDue();
            log.info("外部订单回调任务完成，送达数={}", delivered);
            return delivered;
        }
        catch (RuntimeException exception)
        {
            log.error("外部订单回调任务执行失败", exception);
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
