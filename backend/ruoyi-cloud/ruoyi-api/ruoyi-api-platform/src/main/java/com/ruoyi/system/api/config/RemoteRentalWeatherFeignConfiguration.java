package com.ruoyi.system.api.config;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import feign.Request;

/**
 * 租赁业务天气调用的 Feign 超时配置。
 *
 * <p>天气任务需要按高德城市编码查询并写入所有县级行政区的三日预报，正常运行时间
 * 可能明显超过通用接口的十秒读取超时。该配置只绑定天气专用 Feign 客户端，避免放大
 * 其他租赁业务接口的故障等待时间。</p>
 */
public class RemoteRentalWeatherFeignConfiguration
{
    /** 建立服务连接的最大等待时间。 */
    private static final long CONNECT_TIMEOUT_MILLIS = 10_000L;

    /** 天气刷新任务的最大读取时间，与业务任务分布式锁的最长有效期保持一致。 */
    private static final long READ_TIMEOUT_MILLIS = 30 * 60 * 1_000L;

    /**
     * 创建天气任务专用的 Feign 请求选项。
     *
     * @return 连接和读取超时配置
     */
    @Bean
    public Request.Options remoteRentalWeatherRequestOptions()
    {
        return new Request.Options(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS, READ_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS, true);
    }
}
