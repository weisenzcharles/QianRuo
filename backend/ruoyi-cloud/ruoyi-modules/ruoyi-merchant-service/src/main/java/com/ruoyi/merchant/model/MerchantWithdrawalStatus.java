package com.ruoyi.merchant.model;

/** 提现申请状态。数据库保存数值，接口统一返回中文状态名称。 */
public enum MerchantWithdrawalStatus
{
    PENDING_REVIEW(0, "待审核"), COMPLETED(1, "已完成"), REJECTED(2, "已拒绝"), CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    MerchantWithdrawalStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static MerchantWithdrawalStatus fromCode(Integer code)
    {
        if (code == null) return null;
        for (MerchantWithdrawalStatus item : values()) if (item.code == code) return item;
        return null;
    }
}
