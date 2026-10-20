package com.ruoyi.rentalbusiness.enums;

/** 订单批注所属业务环节。 */
public enum MerchantOrderNoteType
{
    SHIPPING(1),
    RECEIVING(2);

    private final int code;

    MerchantOrderNoteType(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static boolean contains(Integer code)
    {
        for (MerchantOrderNoteType type : values())
        {
            if (type.code == code)
            {
                return true;
            }
        }
        return false;
    }
}
