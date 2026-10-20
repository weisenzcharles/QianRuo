package com.ruoyi.merchant.model;

/** 商户内部通知阅读状态。 */
public enum MerchantInternalNoticeReadStatus
{
    UNREAD(0, "未读"),
    READ(1, "已读");

    private final int code;
    private final String label;

    MerchantInternalNoticeReadStatus(int code, String label)
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

    public static String labelOf(Integer code)
    {
        if (code == null)
        {
            return "未知状态";
        }
        for (MerchantInternalNoticeReadStatus status : values())
        {
            if (status.code == code)
            {
                return status.label;
            }
        }
        return "未知状态";
    }
}
