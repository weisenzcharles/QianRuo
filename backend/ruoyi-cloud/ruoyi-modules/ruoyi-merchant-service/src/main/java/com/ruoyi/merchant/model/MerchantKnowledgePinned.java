package com.ruoyi.merchant.model;

/** 知识文章置顶标记。 */
public enum MerchantKnowledgePinned
{
    NO(0),
    YES(1);

    private final int code;

    MerchantKnowledgePinned(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static MerchantKnowledgePinned fromCode(Integer code)
    {
        if (code == null)
        {
            return NO;
        }
        for (MerchantKnowledgePinned item : values())
        {
            if (item.code == code)
            {
                return item;
            }
        }
        return null;
    }
}
