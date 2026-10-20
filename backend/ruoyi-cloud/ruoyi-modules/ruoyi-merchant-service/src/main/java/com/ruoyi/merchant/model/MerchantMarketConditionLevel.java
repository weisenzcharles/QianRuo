package com.ruoyi.merchant.model;

/** 周转市场统一成色选项。数据库保存稳定编码，页面展示中文标签。 */
public enum MerchantMarketConditionLevel
{
    NEW("NEW", "全新"),
    NINETY_NINE("NINETY_NINE", "99新"),
    NINETY_FIVE("NINETY_FIVE", "95新"),
    NINETY("NINETY", "9成新"),
    EIGHTY_FIVE("EIGHTY_FIVE", "85新"),
    SCRATCHED("SCRATCHED", "有磕碰"),
    REPAIRED("REPAIRED", "有维修史");

    private final String code;
    private final String label;

    MerchantMarketConditionLevel(String code, String label)
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

    /** 按编码或历史中文值解析，兼容已保存的历史帖子。 */
    public static MerchantMarketConditionLevel fromValue(String value)
    {
        if (value == null)
        {
            return null;
        }
        String normalized = value.trim();
        for (MerchantMarketConditionLevel item : values())
        {
            if (item.code.equalsIgnoreCase(normalized) || item.label.equals(normalized))
            {
                return item;
            }
        }
        return null;
    }
}
