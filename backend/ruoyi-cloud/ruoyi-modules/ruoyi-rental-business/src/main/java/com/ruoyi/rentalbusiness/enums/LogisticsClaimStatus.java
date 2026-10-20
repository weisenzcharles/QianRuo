package com.ruoyi.rentalbusiness.enums;

/** 物流赔付单状态。审核与财务入账分开，避免跨服务失败造成假成功。 */
public enum LogisticsClaimStatus
{
    PENDING_REVIEW(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "已驳回"),
    SETTLED(3, "已入账"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    LogisticsClaimStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static LogisticsClaimStatus fromCode(Integer code)
    {
        if (code == null) return null;
        for (LogisticsClaimStatus item : values())
        {
            if (item.code == code.intValue()) return item;
        }
        return null;
    }
}
