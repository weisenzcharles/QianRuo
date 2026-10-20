package com.ruoyi.merchant.model;

/** 周转市场帖子板块。数据库保存数值，接口命令使用枚举名称。 */
public enum MerchantMarketBoardType
{
    SALE(1, "出售"), RENT(2, "出租"), BUY(3, "求购");

    private final int code;
    private final String label;

    MerchantMarketBoardType(int code, String label)
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

    public static MerchantMarketBoardType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantMarketBoardType item : values())
        {
            if (item.code == code)
            {
                return item;
            }
        }
        return null;
    }
}
