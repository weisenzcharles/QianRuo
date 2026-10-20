package com.ruoyi.system.enums;

/** 平台运维告警状态，与运维告警事件表的整型编码保持一致。 */
public enum PlatformAlertEventStatus
{
    PENDING(0, "待处理"),
    ACKNOWLEDGED(1, "已确认"),
    RESOLVED(2, "已解除");

    private final int code;
    private final String label;

    PlatformAlertEventStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformAlertEventStatus fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformAlertEventStatus item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
