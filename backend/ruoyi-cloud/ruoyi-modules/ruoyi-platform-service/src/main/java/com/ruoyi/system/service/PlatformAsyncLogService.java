package com.ruoyi.system.service;

import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ruoyi.common.log.service.AsyncLogService;
import com.ruoyi.system.api.domain.SysOperLog;

/** 平台服务操作日志本地持久化实现。 */
@Primary
@Service
public class PlatformAsyncLogService extends AsyncLogService
{
    private final ISysOperLogService operLogService;

    /**
     * 注入平台操作日志业务服务，避免平台服务通过 Feign 再次调用自身。
     *
     * @param operLogService 平台操作日志业务服务
     */
    public PlatformAsyncLogService(ISysOperLogService operLogService)
    {
        this.operLogService = operLogService;
    }

    /**
     * 异步把平台接口操作日志直接写入平台日志表，阻断远程调用递归链路。
     *
     * @param operLog 待保存平台操作日志
     */
    @Override
    @Async
    public void saveSysLog(SysOperLog operLog)
    {
        operLogService.insertOperlog(operLog);
    }
}
