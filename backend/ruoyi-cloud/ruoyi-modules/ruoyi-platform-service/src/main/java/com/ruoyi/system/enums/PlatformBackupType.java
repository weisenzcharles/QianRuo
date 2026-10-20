package com.ruoyi.system.enums;

/** 平台备份类型，与平台备份记录表的整型编码保持一致。 */
public enum PlatformBackupType
{
    DAILY_DATABASE(1, "数据库每日备份"),
    MONTHLY_LOG_PACKAGE(2, "月度日志包"),
    OBJECT_STORAGE_REPLICA(3, "对象存储副本");

    private final int code;
    private final String label;

    PlatformBackupType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformBackupType fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformBackupType item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
