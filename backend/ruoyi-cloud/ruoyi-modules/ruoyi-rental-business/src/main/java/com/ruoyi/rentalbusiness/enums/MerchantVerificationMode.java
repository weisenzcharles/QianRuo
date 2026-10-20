package com.ruoyi.rentalbusiness.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** 极速核销台操作方向。 */
public enum MerchantVerificationMode
{
    INBOUND("inbound", "扫码入库"),
    OUTBOUND("outbound", "扫码出库");

    private final String code;
    private final String label;

    MerchantVerificationMode(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    @JsonCreator
    public static MerchantVerificationMode fromCode(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantVerificationMode value : values())
        {
            if (value.code.equalsIgnoreCase(code.trim()))
            {
                return value;
            }
        }
        return null;
    }

    @JsonValue
    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }
}
