package com.ruoyi.rentalbusiness.enums;

/** 归还验机结果。 */
public enum MerchantReturnInspectionResult
{
    NORMAL(1, "验机正常"),
    AFTER_SALES(2, "转售后");

    private final Integer code;
    private final String label;

    MerchantReturnInspectionResult(Integer code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public Integer getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public static MerchantReturnInspectionResult fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantReturnInspectionResult value : values())
            {
                if (value.code.equals(code))
                {
                    return value;
                }
            }
        }
        return null;
    }
}
