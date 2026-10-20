package com.ruoyi.merchant.application;

import org.junit.Assert;
import org.junit.Test;

/** 店铺趋势展示天数允许值测试。 */
public class MerchantStoreSettingsServiceTest
{
    @Test
    public void isSupportedDisplayDays_shouldAcceptConfiguredOptionsOnly()
    {
        for (Integer days : new Integer[] { 3, 5, 7, 15, 30 })
        {
            Assert.assertTrue(MerchantStoreSettingsService.isSupportedDisplayDays(days));
        }

        Assert.assertFalse(MerchantStoreSettingsService.isSupportedDisplayDays(1));
        Assert.assertFalse(MerchantStoreSettingsService.isSupportedDisplayDays(90));
        Assert.assertFalse(MerchantStoreSettingsService.isSupportedDisplayDays(null));
    }
}
