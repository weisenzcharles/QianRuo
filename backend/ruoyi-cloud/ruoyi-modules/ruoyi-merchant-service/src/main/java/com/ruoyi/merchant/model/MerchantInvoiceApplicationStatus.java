package com.ruoyi.merchant.model;

/** 发票申请状态。 */
public enum MerchantInvoiceApplicationStatus
{
    PENDING_REVIEW(0, "待审核"), REJECTED(1, "已驳回"), PENDING_ISSUE(2, "待开具"), ISSUED(3, "已开具"),
    RED_FLUSHED(4, "已红冲"), CANCELLED(9, "已取消");

    private final int code;
    private final String label;

    MerchantInvoiceApplicationStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static MerchantInvoiceApplicationStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantInvoiceApplicationStatus item : values())
            {
                if (item.code == code) return item;
            }
        }
        return null;
    }
}
