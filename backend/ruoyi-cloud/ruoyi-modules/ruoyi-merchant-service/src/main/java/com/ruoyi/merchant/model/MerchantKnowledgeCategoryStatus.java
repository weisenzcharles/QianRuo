package com.ruoyi.merchant.model;

/** 知识分类状态。 */
public enum MerchantKnowledgeCategoryStatus
{
    NORMAL(0, "正常"),
    DISABLED(1, "停用");

    private final int code;
    private final String label;

    MerchantKnowledgeCategoryStatus(int code, String label)
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

    public static MerchantKnowledgeCategoryStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantKnowledgeCategoryStatus item : values())
        {
            if (item.code == code)
            {
                return item;
            }
        }
        return null;
    }
}
