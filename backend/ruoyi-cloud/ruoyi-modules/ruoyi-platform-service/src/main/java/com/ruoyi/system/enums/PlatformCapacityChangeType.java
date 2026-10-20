package com.ruoyi.system.enums;

/** 容量快照相对上一次记录的变化类型。 */
public enum PlatformCapacityChangeType
{
    INITIAL(1, "首次记录"),
    INCREASE(2, "容量增加"),
    DECREASE(3, "容量减少"),
    UNCHANGED(4, "无变化");

    private final int code;
    private final String label;

    PlatformCapacityChangeType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformCapacityChangeType fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformCapacityChangeType item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
