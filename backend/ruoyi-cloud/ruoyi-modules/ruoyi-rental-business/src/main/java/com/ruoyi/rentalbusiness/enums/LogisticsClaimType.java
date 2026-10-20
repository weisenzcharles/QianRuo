package com.ruoyi.rentalbusiness.enums;

/** 物流赔付类型。数据库使用整型，浏览器只展示中文。 */
public enum LogisticsClaimType
{
    LATE(1, "晚到"),
    LOST(2, "丢失");

    private final int code;
    private final String label;

    LogisticsClaimType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static LogisticsClaimType fromCode(Integer code)
    {
        if (code == null) return null;
        for (LogisticsClaimType item : values())
        {
            if (item.code == code.intValue()) return item;
        }
        return null;
    }
}
