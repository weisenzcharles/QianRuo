package com.ruoyi.system.enums;

/** 平台运维告警等级，与运维告警事件表的整型编码保持一致。 */
public enum PlatformAlertEventLevel
{
    NOTICE(1, "提示"),
    WARNING(2, "预警"),
    HIGH(3, "高风险");

    private final int code;
    private final String label;

    PlatformAlertEventLevel(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformAlertEventLevel fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformAlertEventLevel item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
