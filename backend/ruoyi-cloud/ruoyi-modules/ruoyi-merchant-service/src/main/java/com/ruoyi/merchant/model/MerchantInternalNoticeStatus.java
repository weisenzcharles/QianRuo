package com.ruoyi.merchant.model;

/** 商户内部通知状态。 */
public enum MerchantInternalNoticeStatus
{
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下架");

    private final int code;
    private final String label;

    MerchantInternalNoticeStatus(int code, String label)
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
        for (MerchantInternalNoticeStatus item : values())
        {
            if (item.code == (code == null ? Integer.MIN_VALUE : code))
            {
                return true;
            }
        }
        return false;
    }

    public static String labelOf(Integer code)
    {
        for (MerchantInternalNoticeStatus item : values())
        {
            if (item.code == (code == null ? Integer.MIN_VALUE : code))
            {
                return item.label;
            }
        }
        return "未知状态";
    }
}
