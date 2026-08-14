package com.ruoyi.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 固定平台后台路由，避免配置中心历史路由将平台请求转发到错误服务。
 *
 * @author qianruo
 */
@Configuration
public class PlatformRouteConfiguration
{
    @Bean
    public RouteLocator platformRouteLocator(RouteLocatorBuilder builder)
    {
        return builder.routes()
                .route("platform-service-fixed", route -> route
                        .order(-101)
                        .path("/admin/**")
                        .filters(filter -> filter.stripPrefix(1))
                        .uri("lb://platform-service"))
                .route("platform-rental-business-fixed", route -> route
                        .order(-100)
                        .path("/platform", "/platform/**")
                        .uri("lb://rental-business"))
                .build();
    }
}
