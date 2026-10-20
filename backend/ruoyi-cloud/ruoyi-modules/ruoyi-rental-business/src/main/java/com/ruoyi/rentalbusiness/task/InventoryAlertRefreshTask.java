package com.ruoyi.rentalbusiness.task;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.service.MerchantInventoryGovernanceService;

/** 统一调度入口调用的库存告警派生事件刷新任务。 */
@Component("inventoryAlertRefreshTask")
public class InventoryAlertRefreshTask
{
    private static final Logger log = LoggerFactory.getLogger(InventoryAlertRefreshTask.class);
    private static final String LOCK_KEY = "rental-business:task:inventory-alert:refresh";
    private static final long LOCK_MINUTES = 15L;

    private final MerchantInventoryGovernanceService governanceService;
    private final RedisService redisService;

    public InventoryAlertRefreshTask(MerchantInventoryGovernanceService governanceService, RedisService redisService)
    {
        this.governanceService = governanceService;
        this.redisService = redisService;
    }

    /** 刷新所有商户的库存告警事件。 */
    public int refresh()
    {
        String lockToken = UUID.randomUUID().toString();
        if (!tryAcquireLock(lockToken))
        {
            log.info("库存告警刷新任务已由其他实例执行，跳过本次任务");
            return 0;
        }
        try
        {
            int changed = governanceService.refreshAllAlertEvents();
            log.info("库存告警刷新任务完成，恢复或新增事件数={}", changed);
            return changed;
        }
        catch (RuntimeException exception)
        {
            log.error("库存告警刷新任务执行失败", exception);
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
