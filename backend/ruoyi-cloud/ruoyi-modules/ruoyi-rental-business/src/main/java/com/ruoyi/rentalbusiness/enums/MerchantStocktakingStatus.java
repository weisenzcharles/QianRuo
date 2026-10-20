package com.ruoyi.rentalbusiness.enums;

/** 盘点单状态。 */
public enum MerchantStocktakingStatus
{
    PROCESSING(0, "盘点中"), COMPLETED(1, "已完成"), CLOSED(2, "已关闭"), REVIEW_REQUIRED(3, "待复核");

    private final int code;
    private final String label;

    MerchantStocktakingStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static MerchantStocktakingStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantStocktakingStatus value : values())
        {
            if (value.code == code)
            {
                return value;
            }
        }
        return null;
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
