package com.ruoyi.merchant.model;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/** 商户店铺设置默认值和枚举契约测试。 */
public class MerchantStoreSettingsVOTest
{
    @Test
    public void defaults_shouldKeepBusinessSafeValues()
    {
        MerchantStoreSettingsVO settings = MerchantStoreSettingsVO.defaults();

        Assert.assertEquals(Integer.valueOf(30), settings.getInboundOutboundTrendDays());
        Assert.assertEquals(Integer.valueOf(30), settings.getInventoryOverviewDays());
        Assert.assertEquals(MerchantInventoryAlertType.COUNT, settings.getInventoryAlertType());
        Assert.assertEquals(MerchantShipTimeSlot.MORNING, settings.getDefaultShipTimeSlot());
        Assert.assertEquals(Arrays.asList("customer", "recipient", "package", "lease", "warehouse", "amount", "remark"),
                settings.getOrderCopyFields());
    }
}
