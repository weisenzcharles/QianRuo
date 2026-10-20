package com.ruoyi.merchant.model;

/** 发票状态枚举。 */
public enum MerchantFinanceInvoiceStatus
{
    PENDING(0, "待开具"), ISSUED(1, "已开具"), REJECTED(2, "已驳回"), RED_FLUSHED(3, "已红冲");

    private final int code;
    private final String label;

    MerchantFinanceInvoiceStatus(int code, String label) { this.code = code; this.label = label; }

    public static String labelOf(Integer code)
    {
        if (code != null) for (MerchantFinanceInvoiceStatus item : values()) if (item.code == code) return item.label;
        return "未申请";
    }
}
