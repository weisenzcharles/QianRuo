package com.ruoyi.common.security.feign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import org.junit.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Feign 客户端消息转换器配置测试。
 */
public class FeignClientMessageConverterConfigurationTest
{
    @Test
    public void customizer_whenConvertersAreEmpty_addsJacksonConverter()
    {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        createCustomizer().accept(converters);

        assertEquals(1, converters.size());
        assertTrue(converters.get(0) instanceof MappingJackson2HttpMessageConverter);
    }

    @Test
    public void customizer_whenJsonConverterExists_doesNotAddDuplicate()
    {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());

        createCustomizer().accept(converters);

        assertEquals(2, converters.size());
    }

    @Test
    public void enableRyFeignClients_usesConverterConfigurationForEveryClient()
    {
        Map<String, Object> attributes = AnnotationMetadata.introspect(FeignEnabledApplication.class)
                .getAnnotationAttributes(EnableFeignClients.class.getName());

        assertNotNull(attributes);
        assertArrayEquals(
                new Class<?>[] { FeignClientMessageConverterConfiguration.class },
                (Class<?>[]) attributes.get("defaultConfiguration"));
    }

    @Test
    public void initializer_eagerlyBuildsConvertersBeforeConcurrentRequests()
    {
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        FeignHttpMessageConverters converters = new FeignHttpMessageConverters(
                beanFactory.getBeanProvider(ResolvableType.forClass(HttpMessageConverter.class)),
                beanFactory.getBeanProvider(HttpMessageConverterCustomizer.class));

        Object initialized = new FeignClientMessageConverterConfiguration()
                .qianruoFeignMessageConvertersInitializer()
                .postProcessAfterInitialization(converters, "feignHttpMessageConverters");

        assertTrue(initialized instanceof FeignHttpMessageConverters);
        assertTrue(((FeignHttpMessageConverters) initialized).getConverters().size() > 0);
    }

    private HttpMessageConverterCustomizer createCustomizer()
    {
        FeignClientMessageConverterConfiguration configuration =
                new FeignClientMessageConverterConfiguration();
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        return configuration.qianruoFeignHttpMessageConverterCustomizer(
                beanFactory.getBeanProvider(ObjectMapper.class));
    }

    @EnableRyFeignClients
    private static class FeignEnabledApplication
    {
    }
}
