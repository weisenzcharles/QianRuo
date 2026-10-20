package com.ruoyi.merchant.model;

/** 周转市场出售和求购使用的固定价格区间。出租板块不允许价格区间。 */
public enum MerchantMarketPriceRange
{
    UNDER_1000("UNDER_1000", "1000元以下"),
    FROM_1000_TO_3000("FROM_1000_TO_3000", "1000-3000元"),
    FROM_3000_TO_5000("FROM_3000_TO_5000", "3000-5000元"),
    FROM_5000_TO_10000("FROM_5000_TO_10000", "5000-10000元"),
    OVER_10000("OVER_10000", "10000元以上");

    private final String code;
    private final String label;

    MerchantMarketPriceRange(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    /** 按编码或历史中文值解析。 */
    public static MerchantMarketPriceRange fromValue(String value)
    {
        if (value == null)
        {
            return null;
        }
        String normalized = value.trim();
        for (MerchantMarketPriceRange item : values())
        {
            if (item.code.equalsIgnoreCase(normalized) || item.label.equals(normalized))
            {
                return item;
            }
        }
        return null;
    }

    public static String labelOf(String value)
    {
        MerchantMarketPriceRange range = fromValue(value);
        return range == null ? value : range.label;
    }
}
