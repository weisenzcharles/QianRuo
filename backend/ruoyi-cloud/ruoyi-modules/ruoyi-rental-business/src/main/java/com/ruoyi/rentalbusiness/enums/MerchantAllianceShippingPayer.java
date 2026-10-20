package com.ruoyi.rentalbusiness.enums;

/** 联盟订单快递费用承担方，与 ry_rental_order_alliance.shipping_payer 保持映射。 */
public enum MerchantAllianceShippingPayer
{
    INITIATOR(1), RESPONDENT(2), NEGOTIATED(3);

    private final int code;

    MerchantAllianceShippingPayer(int code)
    {
        this.code = code;
    }

    public static MerchantAllianceShippingPayer fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAllianceShippingPayer payer : values())
            {
                if (payer.code == code)
                {
                    return payer;
                }
            }
        }
        return null;
    }
}
