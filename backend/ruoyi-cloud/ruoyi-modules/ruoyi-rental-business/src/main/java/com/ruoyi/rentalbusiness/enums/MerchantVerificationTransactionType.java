package com.ruoyi.rentalbusiness.enums;

/** 极速核销台写入的库存流水类型，与库存流水表的受控编码保持一致。 */
public enum MerchantVerificationTransactionType
{
    INBOUND(1, "入库"),
    OUTBOUND(4, "发货"),
    RETURN(5, "归还");

    private final int code;
    private final String label;

    MerchantVerificationTransactionType(int code, String label)
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
