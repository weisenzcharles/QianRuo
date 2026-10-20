package com.ruoyi.system.enums;

/** 平台通用工作台待办状态。 */
public enum PlatformWorkbenchTaskStatus
{
    PENDING(0, "待处理"), PROCESSING(1, "处理中"), ACKNOWLEDGED(2, "已确认");

    private final int code;
    private final String label;

    PlatformWorkbenchTaskStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformWorkbenchTaskStatus fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformWorkbenchTaskStatus item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
