package com.ruoyi.merchant.model;

/** 财务页面查询场景，集中维护页面和流水业务类型的映射。 */
public enum MerchantFinanceScene
{
    REVENUE("revenue"), SHIPPING("shipping"), WITHDRAWAL("withdrawal"), INVOICE("invoice"), BOOK("book");

    private final String code;

    MerchantFinanceScene(String code) { this.code = code; }

    public String getCode() { return code; }

    public static boolean contains(String code)
    {
        for (MerchantFinanceScene scene : values())
        {
            if (scene.code.equals(code)) return true;
        }
        return false;
    }
}
