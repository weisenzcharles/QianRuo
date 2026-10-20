package com.ruoyi.file;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.ruoyi.common.aliyun.oss.config.AliyunOssAutoConfiguration;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import com.ruoyi.system.api.factory.RemoteGlobalConfigFallbackFactory;
import com.ruoyi.system.api.factory.RemoteLogFallbackFactory;

/**
 * 文件服务
 * 
 * @author ruoyi
 */
@EnableRyFeignClients
@Import({ RemoteLogFallbackFactory.class, RemoteGlobalConfigFallbackFactory.class })
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, AliyunOssAutoConfiguration.class })
public class RuoYiFileApplication
{
    /**
     * 启动文件服务，并统一设置服务进程使用亚洲上海时区。
     *
     * @param args Spring Boot 启动参数
     */
    public static void main(String[] args)
    {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(RuoYiFileApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  文件服务模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
