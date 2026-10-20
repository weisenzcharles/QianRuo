package com.ruoyi.rentalbusiness.enums;

/** 盘点扫码校验结果。 */
public enum MerchantStocktakingScanResult
{
    NORMAL(0, "正常"), ABNORMAL(1, "异常");

    private final int code;
    private final String label;

    MerchantStocktakingScanResult(int code, String label)
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
