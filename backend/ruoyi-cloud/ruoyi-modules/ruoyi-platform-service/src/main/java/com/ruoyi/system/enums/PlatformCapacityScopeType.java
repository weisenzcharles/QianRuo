package com.ruoyi.system.enums;

/** 平台容量范围，与容量流水表的整型编码保持一致。 */
public enum PlatformCapacityScopeType
{
    DATABASE(1, "数据库"),
    OBJECT_STORAGE(2, "对象存储"),
    FILE_STORAGE(3, "文件存储");

    private final int code;
    private final String label;

    PlatformCapacityScopeType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformCapacityScopeType fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformCapacityScopeType item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
