package com.ruoyi.rentalbusiness.enums;

/** 库存告警事件状态。 */
public enum MerchantInventoryAlertStatus
{
    PENDING(0, "待处理"),
    ACKNOWLEDGED(1, "已确认"),
    RESOLVED(2, "已恢复");

    private final int code;
    private final String label;

    MerchantInventoryAlertStatus(int code, String label)
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

    public static MerchantInventoryAlertStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantInventoryAlertStatus value : values())
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
