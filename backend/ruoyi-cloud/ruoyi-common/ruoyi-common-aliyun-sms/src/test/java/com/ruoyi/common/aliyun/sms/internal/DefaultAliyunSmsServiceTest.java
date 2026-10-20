package com.ruoyi.common.aliyun.sms.internal;

import java.util.Map;
import org.junit.Test;

/**
 * 短信请求参数校验测试。
 */
public class DefaultAliyunSmsServiceTest
{
    @Test(expected = IllegalArgumentException.class)
    public void sendTemplateSms_invalidPhone_rejectedBeforeSdkCall()
    {
        new DefaultAliyunSmsService(null).sendTemplateSms("not-a-phone", "签名", "SMS_123", Map.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendTemplateSms_missingTemplate_rejectedBeforeSdkCall()
    {
        new DefaultAliyunSmsService(null).sendTemplateSms("+8613800138000", "签名", " ", Map.of());
    }
}
