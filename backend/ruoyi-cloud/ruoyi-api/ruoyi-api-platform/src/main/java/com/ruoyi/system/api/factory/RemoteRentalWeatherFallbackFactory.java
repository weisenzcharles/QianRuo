package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalWeatherService;

/** 租赁业务天气任务内部契约的降级处理。 */
@Component
public class RemoteRentalWeatherFallbackFactory implements FallbackFactory<RemoteRentalWeatherService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteRentalWeatherFallbackFactory.class);

    /**
     * 创建明确失败的天气任务降级实现。
     *
     * <p>天气服务不可用时必须让平台调度日志记录失败，不能返回成功让运维误以为三日天气
     * 已经刷新。</p>
     *
     * @param cause 远程调用失败原因
     * @return 明确返回失败结果的降级实现
     */
    @Override
    public RemoteRentalWeatherService create(Throwable cause)
    {
        log.error("租赁业务天气任务内部调用失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemoteRentalWeatherService()
        {
            /**
             * 返回天气刷新失败结果，让平台调度入口抛出异常并记录失败日志。
             *
             * @param source 内部服务调用标识
             * @return 天气刷新失败结果
             */
            @Override
            public R<Void> refreshThreeDayWeather(String source)
            {
                return R.fail("租赁业务天气服务暂不可用");
            }
        };
    }
}
