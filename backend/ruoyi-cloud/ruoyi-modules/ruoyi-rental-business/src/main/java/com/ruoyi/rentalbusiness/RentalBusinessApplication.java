package com.ruoyi.rentalbusiness;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 租赁核心业务服务启动入口。
 *
 * 该服务只承载商品、套餐、SN、仓库、库存、订单、核销和售后等租赁领域业务，
 * 不承载平台用户、商户用户、角色、菜单或登录逻辑。身份信息通过可信登录上下文
 * 或内部服务契约提供，业务数据由本服务及其专用数据库账号负责维护。
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class RentalBusinessApplication
{
    /**
     * 设置统一业务时区并启动租赁核心业务服务。
     *
     * @param args Spring Boot 启动参数
     */
    public static void main(String[] args)
    {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(RentalBusinessApplication.class, args);
    }
}
