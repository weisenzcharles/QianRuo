package com.ruoyi.common.aliyun.sms;

import org.junit.Test;

/** 运行时短信客户端配置校验测试。 */
public class AliyunSmsServiceFactoryTest
{
    @Test(expected = IllegalArgumentException.class)
    public void create_missingCredential_rejectedBeforeClientConstruction()
    {
        AliyunSmsServiceFactory.create("dysmsapi.aliyuncs.com", "", "secret");
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_urlEndpoint_rejected()
    {
        AliyunSmsServiceFactory.create("https://dysmsapi.aliyuncs.com", "id", "secret");
    }
}
