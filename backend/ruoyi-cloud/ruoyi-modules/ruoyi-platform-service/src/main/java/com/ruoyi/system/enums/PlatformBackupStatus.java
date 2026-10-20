package com.ruoyi.system.enums;

/** 平台备份状态，与平台备份记录表的整型编码保持一致。 */
public enum PlatformBackupStatus
{
    RUNNING(0, "执行中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    EXPIRED(3, "已过期");

    private final int code;
    private final String label;

    PlatformBackupStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformBackupStatus fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformBackupStatus item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
