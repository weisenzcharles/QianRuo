package com.ruoyi.rentalbusiness.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/** 库存告警阈值类型，与库存告警规则表保持数值映射。 */
public enum MerchantInventoryAlertThresholdType
{
    AVAILABLE_PERCENT(1, "可用库存百分比"),
    AVAILABLE_QUANTITY(2, "可用库存数量");

    private final int code;
    private final String label;

    MerchantInventoryAlertThresholdType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    @JsonCreator
    public static MerchantInventoryAlertThresholdType fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantInventoryAlertThresholdType value : values())
            {
                if (value.code == code)
                {
                    return value;
                }
            }
        }
        return null;
    }
}
