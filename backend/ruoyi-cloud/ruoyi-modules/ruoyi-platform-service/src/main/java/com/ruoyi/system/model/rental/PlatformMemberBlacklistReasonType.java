package com.ruoyi.system.model.rental;

/** 商户列入黑名单的原因类型。 */
public enum PlatformMemberBlacklistReasonType
{
    CERTIFICATION(1, "认证资料"),
    FULFILLMENT(2, "履约行为"),
    AFTER_SALES(3, "售后纠纷"),
    FINANCE(4, "财务行为"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    PlatformMemberBlacklistReasonType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public static PlatformMemberBlacklistReasonType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (PlatformMemberBlacklistReasonType value : values())
        {
            if (value.code == code.intValue())
            {
                return value;
            }
        }
        return null;
    }
}
