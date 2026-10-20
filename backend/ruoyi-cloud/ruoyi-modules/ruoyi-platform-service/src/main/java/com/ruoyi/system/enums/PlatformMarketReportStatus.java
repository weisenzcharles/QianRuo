package com.ruoyi.system.enums;

/** 平台周转市场举报处理状态。 */
public enum PlatformMarketReportStatus
{
    PENDING(0, "待处理"), RESOLVED(1, "已处理"), REJECTED(2, "已驳回");

    private final int code;
    private final String label;

    PlatformMarketReportStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformMarketReportStatus fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformMarketReportStatus item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
