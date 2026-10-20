package com.ruoyi.rentalbusiness.enums;

/** 商户租商联盟协助订单页面视图范围。 */
public enum MerchantAllianceTaskView
{
    ALL(null), DIRECT("direct"), PUBLIC_POOL("public_pool");

    private final String code;

    MerchantAllianceTaskView(String code)
    {
        this.code = code;
    }

    public static MerchantAllianceTaskView fromCode(String code)
    {
        if (code == null || code.trim().isEmpty())
        {
            return ALL;
        }
        for (MerchantAllianceTaskView value : values())
        {
            if (code.equalsIgnoreCase(value.code))
            {
                return value;
            }
        }
        return null;
    }

    public String getCode()
    {
        return code;
    }
}
