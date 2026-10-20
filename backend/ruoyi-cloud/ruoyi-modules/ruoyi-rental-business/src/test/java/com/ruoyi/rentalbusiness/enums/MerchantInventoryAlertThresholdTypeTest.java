package com.ruoyi.rentalbusiness.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/** 库存告警阈值类型必须按业务编码而不是枚举下标反序列化。 */
public class MerchantInventoryAlertThresholdTypeTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void deserialize_numericBusinessCode_returnsMatchingType()
            throws Exception
    {
        Assert.assertEquals(MerchantInventoryAlertThresholdType.AVAILABLE_PERCENT,
                objectMapper.readValue("1", MerchantInventoryAlertThresholdType.class));
        Assert.assertEquals(MerchantInventoryAlertThresholdType.AVAILABLE_QUANTITY,
                objectMapper.readValue("2", MerchantInventoryAlertThresholdType.class));
    }

    @Test
    public void deserialize_unknownBusinessCode_returnsNull()
            throws Exception
    {
        Assert.assertNull(objectMapper.readValue("3", MerchantInventoryAlertThresholdType.class));
    }
}
