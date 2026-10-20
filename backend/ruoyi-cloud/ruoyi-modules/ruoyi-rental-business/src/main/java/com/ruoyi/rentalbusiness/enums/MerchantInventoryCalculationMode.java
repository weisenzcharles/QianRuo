package com.ruoyi.rentalbusiness.enums;

/** 可接单数量计算方式。数据库使用数值保存，接口使用枚举名称传输。 */
public enum MerchantInventoryCalculationMode
{
    PROTECTED_QUANTITY(1, "扣除保护数量"),
    TOTAL_PERCENT(2, "总库存比例");

    private final int code;
    private final String label;

    MerchantInventoryCalculationMode(int code, String label)
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
