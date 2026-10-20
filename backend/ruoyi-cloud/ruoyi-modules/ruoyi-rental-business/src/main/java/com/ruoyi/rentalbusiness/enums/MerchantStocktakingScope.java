package com.ruoyi.rentalbusiness.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** 盘点商品范围。 */
public enum MerchantStocktakingScope
{
    ALL("all", 0), CLASS("class", 1), MODEL("model", 2);

    private final String code;
    private final int databaseValue;

    MerchantStocktakingScope(String code, int databaseValue)
    {
        this.code = code;
        this.databaseValue = databaseValue;
    }

    @JsonCreator
    public static MerchantStocktakingScope fromCode(String code)
    {
        if (code != null)
        {
            for (MerchantStocktakingScope value : values())
            {
                if (value.code.equalsIgnoreCase(code.trim()))
                {
                    return value;
                }
            }
        }
        return null;
    }

    @JsonValue
    public String getCode()
    {
        return code;
    }

    public int getDatabaseValue()
    {
        return databaseValue;
    }
}
