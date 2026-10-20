package com.ruoyi.merchant.model;

/** 商户财务账户类型。数据库保存整型，页面使用账户名称。 */
public enum MerchantFinanceAccountType
{
    VALUE_ADDED(1, "增值服务余额"),
    PERFORMANCE_DEPOSIT(2, "履约保证金"),
    RENTAL_SETTLEMENT(3, "租金 / 分账资金");

    private final int code;
    private final String label;

    MerchantFinanceAccountType(int code, String label)
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

    public static MerchantFinanceAccountType fromCode(Integer code)
    {
        if (code == null) return null;
        for (MerchantFinanceAccountType item : values())
        {
            if (item.code == code.intValue()) return item;
        }
        return null;
    }

    public static String labelOf(Integer code)
    {
        MerchantFinanceAccountType item = fromCode(code);
        return item == null ? "未知账户" : item.label;
    }
}
