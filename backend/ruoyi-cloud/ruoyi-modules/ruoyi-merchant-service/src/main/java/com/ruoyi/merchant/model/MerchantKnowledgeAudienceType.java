package com.ruoyi.merchant.model;

/** 商户知识文章受众范围。 */
public enum MerchantKnowledgeAudienceType
{
    ALL(1, "全体员工"),
    SPECIFIED_ROLES(2, "指定员工组");

    private final int code;
    private final String label;

    MerchantKnowledgeAudienceType(int code, String label)
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

    public static MerchantKnowledgeAudienceType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantKnowledgeAudienceType value : values())
        {
            if (value.code == code)
            {
                return value;
            }
        }
        return null;
    }
}
