package com.ruoyi.common.security.config;

import java.text.SimpleDateFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.context.annotation.Bean;

/**
 * 系统配置
 *
 * @author ruoyi
 */
public class ApplicationConfig
{
    public ApplicationConfig()
    {
        DateUtils.useDefaultTimeZone();
    }

    /**
     * 时区配置
     */
    @Bean
    public ObjectMapper objectMapper()
    {
        ObjectMapper objectMapper = JsonMapper.builder().build();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
        dateFormat.setTimeZone(DateUtils.DEFAULT_TIME_ZONE);
        objectMapper.setDateFormat(dateFormat);
        objectMapper.setTimeZone(DateUtils.DEFAULT_TIME_ZONE);
        return objectMapper;
    }
}
