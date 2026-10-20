package com.ruoyi.merchant.model;

/** 商户知识文章及版本状态。数据库保存稳定编码，页面统一展示中文。 */
public enum MerchantKnowledgeStatus
{
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下架");

    private final int code;
    private final String label;

    MerchantKnowledgeStatus(int code, String label)
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

    public static MerchantKnowledgeStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantKnowledgeStatus item : values())
        {
            if (item.code == code)
            {
                return item;
            }
        }
        return null;
    }
}
