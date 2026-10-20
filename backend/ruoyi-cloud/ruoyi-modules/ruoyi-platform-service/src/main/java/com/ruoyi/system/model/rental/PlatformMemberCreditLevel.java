package com.ruoyi.system.model.rental;

/** 商户信用等级。分值越低风险越高，等级只由服务端根据当前分值计算。 */
public enum PlatformMemberCreditLevel
{
    NORMAL(1, "正常"),
    ATTENTION(2, "关注"),
    RISK(3, "风险");

    private final int code;
    private final String label;

    PlatformMemberCreditLevel(int code, String label)
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

    public static PlatformMemberCreditLevel fromScore(int score)
    {
        if (score >= 80)
        {
            return NORMAL;
        }
        if (score >= 60)
        {
            return ATTENTION;
        }
        return RISK;
    }
}
