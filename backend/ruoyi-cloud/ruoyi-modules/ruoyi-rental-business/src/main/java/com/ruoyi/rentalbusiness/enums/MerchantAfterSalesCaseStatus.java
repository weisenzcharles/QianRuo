package com.ruoyi.rentalbusiness.enums;

/** 售后单状态，与 ry_rental_after_sales_case.case_status 保持数值映射。 */
public enum MerchantAfterSalesCaseStatus
{
    PROCESSING(0, "处理中"),
    COMPLETED(1, "已完成"),
    CLOSED(2, "已关闭");

    private final Integer code;
    private final String label;

    MerchantAfterSalesCaseStatus(Integer code, String label)
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

    public static MerchantAfterSalesCaseStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAfterSalesCaseStatus value : values())
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
