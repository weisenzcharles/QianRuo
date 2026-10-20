package com.ruoyi.merchant.application;

import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;

/** 商户注册验证码的格式和核销结果映射测试。 */
public class MerchantRegistrationSmsCodeServiceTest
{
    @Test
    public void normalizePhone_TrimmedValidPhone_ReturnsNormalizedPhone()
    {
        Assert.assertEquals("13800138000", MerchantRegistrationSmsCodeService.normalizePhone(" 13800138000 "));
    }

    @Test
    public void normalizePhone_InvalidPhone_RejectsRequest()
    {
        Assert.assertThrows(ServiceException.class,
                () -> MerchantRegistrationSmsCodeService.normalizePhone("1380013800"));
    }

    @Test
    public void validateVerificationResult_ExpiredCode_ReturnsBusinessError()
    {
        ServiceException exception = Assert.assertThrows(ServiceException.class,
                () -> MerchantRegistrationSmsCodeService.validateVerificationResult(-1L));
        Assert.assertEquals("验证码已过期或尚未发送", exception.getMessage());
    }
}
