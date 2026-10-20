package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.config.RemoteRentalWeatherFeignConfiguration;
import com.ruoyi.system.api.factory.RemoteRentalWeatherFallbackFactory;

/**
 * 平台调度服务访问租赁业务天气任务的专用内部契约。
 *
 * <p>天气刷新可能因为第三方天气服务网络等待而运行较长时间，因此使用独立的 Feign
 * 客户端配置读取超时，不改变仓库、型号等普通租赁业务调用的超时策略。</p>
 */
@FeignClient(contextId = "remoteRentalWeatherService", value = ServiceNameConstants.RENTAL_BUSINESS_SERVICE,
        configuration = RemoteRentalWeatherFeignConfiguration.class,
        fallbackFactory = RemoteRentalWeatherFallbackFactory.class)
public interface RemoteRentalWeatherService
{
    /** 租赁业务服务内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/rental-business";

    /**
     * 触发租赁业务服务刷新全部市级行政区未来三日天气。
     *
     * @param source 内部服务调用标识
     * @return 远程任务执行结果
     */
    @PostMapping(INTERNAL_PREFIX + "/weather/refresh")
    R<Void> refreshThreeDayWeather(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
