package com.ruoyi.common.security.annotation;

import com.ruoyi.common.security.feign.FeignClientMessageConverterConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.AliasFor;
import java.lang.annotation.*;

/**
 * 自定义feign注解
 * 添加basePackages路径
 * 
 * @author ruoyi
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableFeignClients(defaultConfiguration = FeignClientMessageConverterConfiguration.class)
public @interface EnableRyFeignClients
{
    String[] value() default {};

    @AliasFor(annotation = EnableFeignClients.class, attribute = "basePackages")
    String[] basePackages() default { "com.ruoyi" };

    Class<?>[] basePackageClasses() default {};

    @AliasFor(annotation = EnableFeignClients.class, attribute = "defaultConfiguration")
    Class<?>[] defaultConfiguration() default { FeignClientMessageConverterConfiguration.class };

    Class<?>[] clients() default {};
}
