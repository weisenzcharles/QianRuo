package com.ruoyi.merchant.model;

/** 财务流水状态枚举。 */
public enum MerchantFinanceLedgerStatus
{
    PROCESSING(0, "处理中"), SUCCESS(1, "成功"), FAILED(2, "失败"), CLOSED(3, "已关闭");

    private final int code;
    private final String label;

    MerchantFinanceLedgerStatus(int code, String label) { this.code = code; this.label = label; }

    public static String labelOf(Integer code)
    {
        if (code != null) for (MerchantFinanceLedgerStatus item : values()) if (item.code == code) return item.label;
        return "未知状态";
    }
}
