package com.ruoyi.merchant.model;

/** 商户内部通知类型。 */
public enum MerchantInternalNoticeType
{
    NOTIFICATION(1, "通知"),
    ANNOUNCEMENT(2, "公告");

    private final int code;
    private final String label;

    MerchantInternalNoticeType(int code, String label)
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
        return NOTIFICATION.code == (code == null ? Integer.MIN_VALUE : code)
                || ANNOUNCEMENT.code == (code == null ? Integer.MIN_VALUE : code);
    }

    public static String labelOf(Integer code)
    {
        return NOTIFICATION.code == (code == null ? Integer.MIN_VALUE : code)
                ? NOTIFICATION.label
                : ANNOUNCEMENT.code == (code == null ? Integer.MIN_VALUE : code)
                        ? ANNOUNCEMENT.label : "未知类型";
    }
}
