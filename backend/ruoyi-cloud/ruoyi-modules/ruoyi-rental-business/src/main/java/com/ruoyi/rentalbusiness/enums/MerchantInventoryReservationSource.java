package com.ruoyi.rentalbusiness.enums;

/** 库存预占来源。 */
public enum MerchantInventoryReservationSource
{
    INTERNAL(1, "内部订单"),
    EXTERNAL(2, "外部平台");

    private final int code;
    private final String label;

    MerchantInventoryReservationSource(int code, String label)
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
}
