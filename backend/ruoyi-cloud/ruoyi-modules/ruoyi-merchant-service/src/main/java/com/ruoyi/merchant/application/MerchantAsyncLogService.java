package com.ruoyi.merchant.application;

import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.ruoyi.common.log.service.AsyncLogService;
import com.ruoyi.system.api.domain.SysOperLog;

/** 将商户服务的注解日志写入商户自有日志表。 */
@Primary
@Service
public class MerchantAsyncLogService extends AsyncLogService
{
    private final MerchantOperLogService operLogService;

    public MerchantAsyncLogService(MerchantOperLogService operLogService)
    {
        this.operLogService = operLogService;
    }

    @Override
    @Async
    public void saveSysLog(SysOperLog operLog)
    {
        operLogService.record(operLog);
    }
}
