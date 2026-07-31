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

    /**
     * 注入商户操作日志服务，使通用审计切面写入商户自有日志表。
     *
     * @param operLogService 商户操作日志服务
     */
    public MerchantAsyncLogService(MerchantOperLogService operLogService)
    {
        this.operLogService = operLogService;
    }

    /**
     * 异步保存商户接口操作日志，匿名公开接口允许租户和商户字段为空。
     *
     * @param operLog 通用操作审计对象
     */
    @Override
    @Async
    public void saveSysLog(SysOperLog operLog)
    {
        operLogService.record(operLog);
    }
}
