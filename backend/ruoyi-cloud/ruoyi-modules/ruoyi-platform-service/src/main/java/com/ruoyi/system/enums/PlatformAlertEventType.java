package com.ruoyi.system.enums;

/** 平台运维告警类型，与运维告警事件表的整型编码保持一致。 */
public enum PlatformAlertEventType
{
    SCHEDULED_TASK(1, "调度任务"),
    EXTERNAL_INTERFACE(2, "外部接口"),
    PAYMENT(3, "支付"),
    STORAGE(4, "存储"),
    SYSTEM(5, "系统");

    private final int code;
    private final String label;

    PlatformAlertEventType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformAlertEventType fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformAlertEventType item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
