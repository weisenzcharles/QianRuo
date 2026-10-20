package com.ruoyi.rentalbusiness.enums;

/** 库存预测风险等级。 */
public enum MerchantInventoryForecastRiskLevel
{
    NORMAL(0, "正常"),
    LOW(1, "低库存"),
    UNAVAILABLE(2, "无可用库存");

    private final int code;
    private final String label;

    MerchantInventoryForecastRiskLevel(int code, String label)
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

    public static MerchantInventoryForecastRiskLevel fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantInventoryForecastRiskLevel value : values())
        {
            if (value.code == code)
            {
                return value;
            }
        }
        return null;
    }
}
