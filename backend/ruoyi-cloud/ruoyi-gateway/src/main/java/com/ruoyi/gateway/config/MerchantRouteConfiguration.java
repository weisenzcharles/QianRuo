package com.ruoyi.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商户系统固定路由。
 *
 * 该安全边界不依赖 Nacos 配置是否已刷新，避免商户接口被网关当作静态资源处理。
 *
 * @author qianruo
 */
@Configuration
public class MerchantRouteConfiguration
{
    @Bean
    public RouteLocator merchantSystemRouteLocator(RouteLocatorBuilder builder)
    {
        return builder.routes()
                .route("merchant-domain-static", route -> route
                        .path("/merchant/**")
                        .uri("lb://merchant-service"))
                .build();
    }
}
