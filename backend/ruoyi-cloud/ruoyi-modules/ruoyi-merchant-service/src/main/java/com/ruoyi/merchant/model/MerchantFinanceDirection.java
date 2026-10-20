package com.ruoyi.merchant.model;

/** 财务流水方向枚举。数据库保留整型，页面统一显示中文。 */
public enum MerchantFinanceDirection
{
    INCOME(1, "收入"), EXPENSE(2, "支出"), FREEZE(3, "冻结"), UNFREEZE(4, "解冻"), REVERSE(5, "冲正");

    private final int code;
    private final String label;

    MerchantFinanceDirection(int code, String label) { this.code = code; this.label = label; }
    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static String labelOf(Integer code)
    {
        if (code != null) for (MerchantFinanceDirection item : values()) if (item.code == code) return item.label;
        return "未知方向";
    }
}
