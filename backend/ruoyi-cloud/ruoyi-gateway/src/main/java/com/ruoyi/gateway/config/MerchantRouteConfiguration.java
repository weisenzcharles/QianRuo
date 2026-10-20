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
                .route("inventory-sync-open", route -> route
                        .order(-210)
                        .path("/open/inventory", "/open/inventory/**", "/open/orders", "/open/orders/**")
                        .uri("lb://rental-business"))
                .route("merchant-express-company-business", route -> route
                        .order(-200)
                        .path("/merchant/express-companies", "/merchant/express-companies/**")
                        .uri("lb://rental-business"))
                .route("merchant-order-business", route -> route
                        .order(-100)
                        .path("/merchant/orders", "/merchant/orders/**")
                        .uri("lb://rental-business"))
                .route("merchant-alliance-business", route -> route
                        .order(-95)
                        .path("/merchant/alliance-scopes", "/merchant/alliance-scopes/**",
                                "/merchant/alliance-disputes", "/merchant/alliance-disputes/**")
                        .uri("lb://rental-business"))
                .route("merchant-master-data-business", route -> route
                        .order(-90)
                        .path("/merchant/warehouse", "/merchant/warehouse/**",
                                "/merchant/region", "/merchant/region/**",
                                "/merchant/product", "/merchant/product/**",
                                "/merchant/sn", "/merchant/sn/**",
                                "/merchant/accessory", "/merchant/accessory/**",
                                "/merchant/package", "/merchant/package/**",
                                "/merchant/inventory", "/merchant/inventory/**",
                                "/merchant/amap", "/merchant/amap/**",
                                "/merchant/express-companies", "/merchant/express-companies/**")
                        .uri("lb://rental-business"))
                .route("merchant-workflow-business", route -> route
                        .order(-80)
                        .path("/merchant/inventory-check", "/merchant/inventory-check/**",
                                "/merchant/after-sales", "/merchant/after-sales/**",
                                "/merchant/express-time-estimates", "/merchant/express-time-estimates/**",
                                "/merchant/order-channel", "/merchant/order-channel/**",
                                "/merchant/batch", "/merchant/batch/**",
                                "/merchant/file-quota", "/merchant/file-quota/**",
                                "/merchant/logistics", "/merchant/logistics/**",
                                "/merchant/work", "/merchant/work/**",
                                "/merchant/protocol", "/merchant/protocol/**",
                                "/merchant/verification", "/merchant/verification/**")
                        .uri("lb://rental-business"))
                .route("merchant-domain-static", route -> route
                        .path("/merchant/**")
                        .uri("lb://merchant-service"))
                .build();
    }
}
