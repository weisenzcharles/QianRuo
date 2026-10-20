package com.ruoyi.common.security.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import feign.RequestInterceptor;

/**
 * Feign 配置注册
 *
 * @author ruoyi
 **/
@Configuration
public class FeignAutoConfiguration
{
    @Bean
    public RequestInterceptor requestInterceptor()
    {
        return new FeignRequestInterceptor();
    }

    /**
     * 注册所有 Feign 客户端共用的 Jackson JSON 转换器。
     *
     * <p>项目使用 Spring Boot 4 和 Spring Cloud OpenFeign 5，Feign 的默认转换器由
     * Web MVC 自动配置提供。在只引入公共模块、没有完整 Web MVC 自动配置的业务服务中，
     * 默认转换器集合可能为空，进而导致内部接口请求体无法编码或响应无法解码。显式注册
     * 一个使用系统统一 ObjectMapper 的转换器，确保 DTO、R 响应和日志实体在所有微服务
     * 的 Feign 子上下文中都能使用同一套 JSON 规则。</p>
     *
     * @param objectMapper 系统统一 JSON 映射器
     * @return Feign 使用的 Jackson JSON 转换器
     */
    @Bean
    @ConditionalOnMissingBean(name = "qianruoFeignJsonHttpMessageConverter")
    public HttpMessageConverter<Object> qianruoFeignJsonHttpMessageConverter(ObjectMapper objectMapper)
    {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
