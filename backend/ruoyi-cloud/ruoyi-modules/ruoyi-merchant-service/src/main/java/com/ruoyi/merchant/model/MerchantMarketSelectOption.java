package com.ruoyi.merchant.model;

/** 周转市场下拉选项，不向前端暴露数据库主键。 */
public class MerchantMarketSelectOption
{
    private String value;
    private String label;

    public MerchantMarketSelectOption()
    {
    }

    public MerchantMarketSelectOption(String value, String label)
    {
        this.value = value;
        this.label = label;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }
}
