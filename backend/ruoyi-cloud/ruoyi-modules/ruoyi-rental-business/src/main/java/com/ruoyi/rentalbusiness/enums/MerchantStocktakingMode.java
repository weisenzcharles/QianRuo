package com.ruoyi.rentalbusiness.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** 盘点操作方式。 */
public enum MerchantStocktakingMode
{
    MANUAL("manual", 0, "手工盘点"), SCAN("scan", 1, "扫码盘点");

    private final String code;
    private final int databaseValue;
    private final String label;

    MerchantStocktakingMode(String code, int databaseValue, String label)
    {
        this.code = code;
        this.databaseValue = databaseValue;
        this.label = label;
    }

    @JsonCreator
    public static MerchantStocktakingMode fromCode(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantStocktakingMode value : values())
        {
            if (value.code.equalsIgnoreCase(code.trim()))
            {
                return value;
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

    public String getLabel()
    {
        return label;
    }
}
