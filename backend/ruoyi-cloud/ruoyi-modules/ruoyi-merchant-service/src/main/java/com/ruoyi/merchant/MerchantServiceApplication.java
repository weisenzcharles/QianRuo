package com.ruoyi.merchant;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 独立商户服务启动入口。
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class MerchantServiceApplication
{
    public static void main(String[] args)
    {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(MerchantServiceApplication.class, args);
    }
}
