package com.ruoyi.rentalbusiness.enums;

/** 库存告警规则状态。 */
public enum MerchantInventoryAlertRuleStatus
{
    ENABLED(0, "启用"),
    DISABLED(1, "停用");

    private final int code;
    private final String label;

    MerchantInventoryAlertRuleStatus(int code, String label)
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

    public static MerchantInventoryAlertRuleStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantInventoryAlertRuleStatus value : values())
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
