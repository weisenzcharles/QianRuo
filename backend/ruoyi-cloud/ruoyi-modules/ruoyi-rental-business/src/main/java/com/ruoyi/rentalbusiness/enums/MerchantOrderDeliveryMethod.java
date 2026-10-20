package com.ruoyi.rentalbusiness.enums;

/** 商户订单配送方式。 */
public enum MerchantOrderDeliveryMethod
{
    EXPRESS("express", "快递"),
    FLASH("flash", "闪送"),
    DOOR_TO_DOOR("door_to_door", "上门");

    private final String code;
    private final String label;

    MerchantOrderDeliveryMethod(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    /** 按持久化编码查找配送方式。 */
    public static MerchantOrderDeliveryMethod fromCode(String code)
    {
        for (MerchantOrderDeliveryMethod method : values())
        {
            if (method.code.equals(code))
            {
                return method;
            }
        }
        return null;
    }
}
