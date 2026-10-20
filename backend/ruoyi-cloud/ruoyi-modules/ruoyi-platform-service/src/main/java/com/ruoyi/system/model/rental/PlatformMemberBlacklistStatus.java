package com.ruoyi.system.model.rental;

/** 商户黑名单记录状态。 */
public enum PlatformMemberBlacklistStatus
{
    ACTIVE(1, "生效中"),
    RELEASED(2, "已解除");

    private final int code;
    private final String label;

    PlatformMemberBlacklistStatus(int code, String label)
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

    public static PlatformMemberBlacklistStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (PlatformMemberBlacklistStatus value : values())
        {
            if (value.code == code.intValue())
            {
                return value;
            }
        }
        return null;
    }
}
