package com.ruoyi.system.model.rental;

/** 商户信用事件类型。 */
public enum PlatformMemberCreditEventType
{
    MANUAL_ADJUSTMENT(1, "人工调整"),
    FULFILLMENT(2, "履约行为"),
    AFTER_SALES(3, "售后纠纷"),
    FINANCE(4, "财务行为"),
    POSITIVE(5, "正向行为"),
    APPEAL_REVERSAL(6, "申诉冲正");

    private final int code;
    private final String label;

    PlatformMemberCreditEventType(int code, String label)
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

    public static PlatformMemberCreditEventType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (PlatformMemberCreditEventType value : values())
        {
            if (value.code == code.intValue())
            {
                return value;
            }
        }
        return null;
    }
}
