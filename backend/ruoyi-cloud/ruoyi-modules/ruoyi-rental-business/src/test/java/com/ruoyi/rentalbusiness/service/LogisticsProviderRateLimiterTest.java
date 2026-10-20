package com.ruoyi.rentalbusiness.service;

import org.junit.Assert;
import org.junit.Test;

/** 物流供应商秒级限流边界测试。 */
public class LogisticsProviderRateLimiterTest
{
    @Test
    public void isWithinLimit_十次以内允许_超过后排队()
    {
        Assert.assertTrue(LogisticsProviderRateLimiter.isWithinLimit(null));
        Assert.assertTrue(LogisticsProviderRateLimiter.isWithinLimit(1L));
        Assert.assertTrue(LogisticsProviderRateLimiter.isWithinLimit(10L));
        Assert.assertFalse(LogisticsProviderRateLimiter.isWithinLimit(11L));
    }
}
