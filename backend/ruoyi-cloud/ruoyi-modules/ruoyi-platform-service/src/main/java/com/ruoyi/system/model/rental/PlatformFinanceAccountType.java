package com.ruoyi.system.model.rental;

/** 平台财务分户类型，与租赁财务表中的整型编码保持一致。 */
public enum PlatformFinanceAccountType
{
    VALUE_ADDED(1, "增值服务余额"),
    PERFORMANCE_DEPOSIT(2, "履约保证金"),
    RENTAL_SETTLEMENT(3, "租金 / 分账资金");

    private final int code;
    private final String label;

    PlatformFinanceAccountType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static PlatformFinanceAccountType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (PlatformFinanceAccountType item : values())
        {
            if (item.code == code.intValue())
            {
                return item;
            }
        }
        return null;
    }

    public static String labelOf(Integer code)
    {
        PlatformFinanceAccountType item = fromCode(code);
        return item == null ? "未知账户" : item.label;
    }
}
