package com.ruoyi.rentalbusiness.enums;

/** 联盟订单配送方式，与 ry_rental_order_alliance.shipping_mode 保持映射。 */
public enum MerchantAllianceShippingMode
{
    EXPRESS(1), SELF_PICKUP(2), DELIVERY(3), OFFLINE(4);

    private final int code;

    MerchantAllianceShippingMode(int code)
    {
        this.code = code;
    }

    public static MerchantAllianceShippingMode fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAllianceShippingMode mode : values())
            {
                if (mode.code == code)
                {
                    return mode;
                }
            }
        }
        return null;
    }
}
