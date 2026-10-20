package com.ruoyi.common.aliyun.sms.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.ruoyi.common.aliyun.sms.AliyunSmsService;
import com.ruoyi.common.aliyun.sms.internal.DefaultAliyunSmsService;

/**
 * 短信公共客户端自动配置；未显式启用时不创建客户端。
 */
@AutoConfiguration
@EnableConfigurationProperties(AliyunSmsProperties.class)
@ConditionalOnProperty(prefix = "aliyun.sms", name = "enabled", havingValue = "true")
public class AliyunSmsAutoConfiguration
{
    @Bean
    @ConditionalOnMissingBean(AliyunSmsService.class)
    public AliyunSmsService aliyunSmsService(AliyunSmsProperties properties) throws Exception
    {
        properties.validate();
        Config config = new Config()
                .setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret());
        config.endpoint = properties.getEndpoint();
        Client client = new Client(config);
        return new DefaultAliyunSmsService(client);
    }
}
