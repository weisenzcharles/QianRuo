package com.ruoyi.system.model.rental;

/** 平台提现审核状态。 */
public enum PlatformFinanceWithdrawalStatus
{
    PENDING_REVIEW(0, "待审核"), COMPLETED(1, "已完成"), REJECTED(2, "已拒绝"), CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    PlatformFinanceWithdrawalStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformFinanceWithdrawalStatus fromCode(Integer code)
    {
        if (code == null) return null;
        for (PlatformFinanceWithdrawalStatus item : values()) if (item.code == code) return item;
        return null;
    }
}
