package com.ruoyi.system.enums;

/** 平台回收站记录状态。 */
public enum PlatformRecycleStatus
{
    PENDING(0, "待恢复"), RESTORED(1, "已恢复"), CLEANED(2, "已清理");

    private final int code;
    private final String label;

    PlatformRecycleStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static PlatformRecycleStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (PlatformRecycleStatus value : values())
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
