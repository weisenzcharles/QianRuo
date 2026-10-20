package com.ruoyi.system.enums;

/** 平台客服工单追加事件类型。 */
public enum PlatformTicketEventType
{
    CREATED(1, "创建"), ASSIGNED(2, "分配"), SUPPLEMENTED(3, "补充材料"),
    PROCESSED(4, "处理"), REVIEWED(5, "审核"), STATUS_CHANGED(6, "状态变更"), NOTE(9, "备注");

    private final int code;
    private final String label;

    PlatformTicketEventType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static PlatformTicketEventType fromCode(Integer code)
    {
        if (code != null)
        {
            for (PlatformTicketEventType value : values())
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
