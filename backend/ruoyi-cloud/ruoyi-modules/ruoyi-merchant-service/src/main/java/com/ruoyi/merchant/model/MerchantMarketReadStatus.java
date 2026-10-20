package com.ruoyi.merchant.model;

/** 周转市场私信已读状态。数据库保存数值，接口使用受控枚举名称。 */
public enum MerchantMarketReadStatus
{
    UNREAD(0, "未读"), READ(1, "已读");

    private final int code;
    private final String label;

    MerchantMarketReadStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static MerchantMarketReadStatus fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (MerchantMarketReadStatus value : values())
        {
            if (value.code == code)
                return value;
        }
        return null;
    }
}
