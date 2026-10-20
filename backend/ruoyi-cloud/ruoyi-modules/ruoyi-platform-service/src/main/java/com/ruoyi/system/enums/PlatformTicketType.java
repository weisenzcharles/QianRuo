package com.ruoyi.system.enums;

/** 平台统一客服工单类型。 */
public enum PlatformTicketType
{
    CERTIFICATION(1, "认证"), AFTER_SALES(2, "售后"), FINANCE(3, "财务"), ORDER(4, "订单"),
    CONTENT(5, "内容"), OTHER(9, "其他");

    private final int code;
    private final String label;

    PlatformTicketType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static PlatformTicketType fromCode(Integer code)
    {
        if (code != null)
        {
            for (PlatformTicketType value : values())
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
