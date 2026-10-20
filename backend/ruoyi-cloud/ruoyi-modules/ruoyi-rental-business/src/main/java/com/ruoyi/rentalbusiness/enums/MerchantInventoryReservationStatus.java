package com.ruoyi.rentalbusiness.enums;

/** 库存预占状态。 */
public enum MerchantInventoryReservationStatus
{
    RESERVED(1, "已预占"),
    RELEASED(2, "已释放"),
    CONSUMED(3, "已消耗"),
    EXPIRED(4, "已过期");

    private final int code;
    private final String label;

    MerchantInventoryReservationStatus(int code, String label)
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

    public static MerchantInventoryReservationStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantInventoryReservationStatus value : values())
        {
            if (value.code == code)
            {
                return value;
            }
        }
        return null;
    }
}
