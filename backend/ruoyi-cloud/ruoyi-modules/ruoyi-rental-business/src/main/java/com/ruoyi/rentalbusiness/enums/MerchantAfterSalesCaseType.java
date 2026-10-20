package com.ruoyi.rentalbusiness.enums;

/** 售后单类型，与 ry_rental_after_sales_case.case_type 保持数值映射。 */
public enum MerchantAfterSalesCaseType
{
    DAMAGE(1, "损坏"),
    LOST(2, "丢失"),
    COMPENSATION(3, "赔付"),
    OTHER(9, "其他");

    private final Integer code;
    private final String label;

    MerchantAfterSalesCaseType(Integer code, String label)
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

    public static MerchantAfterSalesCaseType fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAfterSalesCaseType value : values())
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
