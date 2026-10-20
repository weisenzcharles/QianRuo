package com.ruoyi.system.enums;

/** 平台统一客服工单优先级。 */
public enum PlatformTicketPriority
{
    NORMAL(1, "普通"), IMPORTANT(2, "重要"), URGENT(3, "紧急");

    private final int code;
    private final String label;

    PlatformTicketPriority(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static PlatformTicketPriority fromCode(Integer code)
    {
        if (code != null)
        {
            for (PlatformTicketPriority value : values())
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
