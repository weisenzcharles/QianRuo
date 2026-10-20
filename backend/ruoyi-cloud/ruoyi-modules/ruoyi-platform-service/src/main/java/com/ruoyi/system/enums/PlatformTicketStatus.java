package com.ruoyi.system.enums;

/** 平台统一客服工单状态。 */
public enum PlatformTicketStatus
{
    UNASSIGNED(0, "待分配"), PROCESSING(1, "处理中"), WAITING_SUPPLEMENT(2, "待补充"),
    WAITING_REVIEW(3, "待审核"), WAITING_CONFIRM(4, "待确认"), RESOLVED(5, "已解决"),
    REJECTED(6, "已驳回"), CLOSED(7, "已关闭");

    private final int code;
    private final String label;

    PlatformTicketStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static PlatformTicketStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (PlatformTicketStatus value : values())
            {
                if (value.code == code)
                    return value;
            }
        }
        return null;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
}
