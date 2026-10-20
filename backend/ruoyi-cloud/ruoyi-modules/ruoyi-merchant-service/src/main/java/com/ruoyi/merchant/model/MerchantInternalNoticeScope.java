package com.ruoyi.merchant.model;

/** 商户内部通知投放范围。 */
public enum MerchantInternalNoticeScope
{
    ALL_USERS(1, "全体员工"),
    SPECIFIED_ROLES(2, "指定员工组");

    private final int code;
    private final String label;

    MerchantInternalNoticeScope(int code, String label)
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

    public static boolean contains(Integer code)
    {
        return ALL_USERS.code == (code == null ? Integer.MIN_VALUE : code)
                || SPECIFIED_ROLES.code == (code == null ? Integer.MIN_VALUE : code);
    }

    public static String labelOf(Integer code)
    {
        return ALL_USERS.code == (code == null ? Integer.MIN_VALUE : code)
                ? ALL_USERS.label
                : SPECIFIED_ROLES.code == (code == null ? Integer.MIN_VALUE : code)
                        ? SPECIFIED_ROLES.label : "未知范围";
    }
}
