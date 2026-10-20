package com.ruoyi.auth.service;

import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;
import static org.junit.Assert.assertEquals;

/** 商户短信验证码输入与一次性校验结果测试。 */
public class MerchantSmsLoginCodeServiceTest
{
    @Test
    public void normalizePhone_trimsWhitespace()
    {
        assertEquals("13800138000", MerchantSmsLoginCodeService.normalizePhone(" 13800138000 "));
    }

    @Test(expected = ServiceException.class)
    public void normalizePhone_rejectsInvalidNumber()
    {
        MerchantSmsLoginCodeService.normalizePhone("1380013800");
    }

    @Test
    public void maskPhone_hidesMiddleDigits()
    {
        assertEquals("138****8000", MerchantSmsLoginCodeService.maskPhone("13800138000"));
    }

    @Test
    public void maskPhone_returnsSafeLabelForInvalidInput()
    {
        assertEquals("未知手机号", MerchantSmsLoginCodeService.maskPhone(null));
    }

    @Test
    public void validateVerificationResult_consumedCodeAccepted()
    {
        MerchantSmsLoginCodeService.validateVerificationResult(1L);
    }

    @Test(expected = ServiceException.class)
    public void validateVerificationResult_wrongCodeRejected()
    {
        MerchantSmsLoginCodeService.validateVerificationResult(0L);
    }

    @Test(expected = ServiceException.class)
    public void validateVerificationResult_expiredCodeRejected()
    {
        MerchantSmsLoginCodeService.validateVerificationResult(-1L);
    }

    @Test(expected = ServiceException.class)
    public void validateVerificationResult_tooManyAttemptsRejected()
    {
        MerchantSmsLoginCodeService.validateVerificationResult(-2L);
    }
}
