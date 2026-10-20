package com.ruoyi.common.security.feign;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Feign 客户端消息转换器配置。
 *
 * <p>Feign 使用独立的命名子上下文，主应用上下文中的 MVC 转换器不会稳定地成为
 * 子上下文的本地 Bean。此配置作为所有 Feign 客户端的默认配置载入子上下文，确保
 * Spring Cloud OpenFeign 5 在默认转换器集合为空时仍具备 JSON 编解码能力。</p>
 *
 * @author ruoyi
 */
@Configuration(proxyBeanMethods = false)
public class FeignClientMessageConverterConfiguration
{
    /**
     * 在 Feign 子上下文中注册本地 JSON 转换器。
     *
     * <p>不能只依赖父上下文的转换器 Bean：OpenFeign 会在命名子上下文中重新创建
     * {@code FeignHttpMessageConverters}，而某些服务的父上下文没有 MVC 默认转换器。</p>
     *
     * @param objectMapperProvider 项目统一 JSON 映射器提供器
     * @return Feign 使用的 Jackson 2 JSON 转换器
     */
    @Bean
    public HttpMessageConverter<Object> qianruoFeignJsonHttpMessageConverter(
            ObjectProvider<ObjectMapper> objectMapperProvider)
    {
        return new MappingJackson2HttpMessageConverter(
                objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    /**
     * 为 Feign 转换器集合补充项目统一的 Jackson 2 JSON 转换器。
     *
     * @param objectMapperProvider 项目统一 JSON 映射器提供器
     * @return Feign 消息转换器定制器
     */
    @Bean
    public HttpMessageConverterCustomizer qianruoFeignHttpMessageConverterCustomizer(
            ObjectProvider<ObjectMapper> objectMapperProvider)
    {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return converters -> {
            if (!supportsJson(converters))
            {
                converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
            }
        };
    }

    /**
     * 在 Feign 子上下文刷新期间完成转换器集合初始化。
     *
     * <p>OpenFeign 5 的 {@link FeignHttpMessageConverters} 使用非同步懒加载。首页并发
     * 发起多个内部请求时，其他线程可能在集合创建后、填充前读取到空列表，最终由
     * {@code HttpMessageConverterExtractor} 抛出 {@code 'messageConverters' must not be empty}。
     * 提前初始化可以关闭这个并发窗口，同时保留 OpenFeign 的默认转换器组装逻辑。</p>
     *
     * @return Feign 转换器初始化后置处理器
     */
    @Bean
    public BeanPostProcessor qianruoFeignMessageConvertersInitializer()
    {
        return new BeanPostProcessor()
        {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName)
            {
                if (bean instanceof FeignHttpMessageConverters converters)
                {
                    converters.getConverters();
                }
                return bean;
            }
        };
    }

    private boolean supportsJson(List<HttpMessageConverter<?>> converters)
    {
        return converters.stream()
                .flatMap(converter -> converter.getSupportedMediaTypes().stream())
                .anyMatch(MediaType.APPLICATION_JSON::isCompatibleWith);
    }
}
