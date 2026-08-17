package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalWeatherService;

/**
 * 平台调度服务中的市级天气刷新任务入口。
 *
 * <p>该类只负责把平台调度命令转发到 rental-business 的内部契约，天气查询、请求去重、
 * 分布式锁和数据库写入全部由租赁业务服务完成。类名位于 {@code com.ruoyi.job.task}
 * 白名单内，因此可以作为平台定时任务的调用目标。</p>
 */
@Component("regionWeatherRefreshJob")
public class RegionWeatherRefreshJob
{
    private static final Logger log = LoggerFactory.getLogger(RegionWeatherRefreshJob.class);

    private final RemoteRentalWeatherService remoteRentalWeatherService;

    /**
     * 注入租赁业务服务内部 Feign 契约。
     *
     * @param remoteRentalWeatherService 租赁业务天气任务内部调用客户端
     */
    public RegionWeatherRefreshJob(RemoteRentalWeatherService remoteRentalWeatherService)
    {
        this.remoteRentalWeatherService = remoteRentalWeatherService;
    }

    /**
     * 调用租赁业务服务刷新全部市级行政区未来三日天气。
     *
     * <p>使用 {@code from-source=inner} 完成内部鉴权；远程调用异常或业务返回失败时主动
     * 抛出异常，让平台调度日志准确记录失败，不能把降级结果当作成功。</p>
     *
     * @throws IllegalStateException 远程服务不可用或返回失败时抛出
     */
    public void refreshThreeDayWeather()
    {
        log.info("平台天气定时任务开始调用 rental-business 三日天气刷新接口");
        R<Void> response = remoteRentalWeatherService.refreshThreeDayWeather(SecurityConstants.INNER);
        if (response == null)
        {
            log.error("平台天气定时任务调用失败：rental-business 返回空响应");
            throw new IllegalStateException("rental-business 天气刷新接口返回空响应");
        }
        if (!R.isSuccess(response))
        {
            log.error("平台天气定时任务调用失败：code={}, message={}", response.getCode(), response.getMsg());
            throw new IllegalStateException("rental-business 天气刷新失败：" + response.getMsg());
        }
        log.info("平台天气定时任务调用完成，rental-business 已接收并执行三日天气刷新");
    }
}
