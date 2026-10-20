package com.ruoyi.merchant.model;

/** 知识文章来源类型。 */
public enum MerchantKnowledgeSourceType
{
    PLATFORM(1, "平台公共"),
    MERCHANT(2, "商户自建");

    private final int code;
    private final String label;

    MerchantKnowledgeSourceType(int code, String label)
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

    public static MerchantKnowledgeSourceType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantKnowledgeSourceType item : values())
        {
            if (item.code == code)
            {
                return item;
            }
        }
        return null;
    }
}
