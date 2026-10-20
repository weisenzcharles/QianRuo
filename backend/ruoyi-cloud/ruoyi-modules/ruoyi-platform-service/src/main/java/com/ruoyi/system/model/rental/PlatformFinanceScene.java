package com.ruoyi.system.model.rental;

/** 平台财务页面与流水筛选场景。 */
public enum PlatformFinanceScene
{
    VALUE_ADDED("value-added"),
    SHIPPING("shipping"),
    WITHDRAWAL("withdrawal"),
    INVOICE("invoice"),
    BOOK("book");

    private final String code;

    PlatformFinanceScene(String code)
    {
        this.code = code;
    }

    public static boolean contains(String value)
    {
        for (PlatformFinanceScene item : values())
        {
            if (item.code.equals(value))
            {
                return true;
            }
        }
        return false;
    }
}
