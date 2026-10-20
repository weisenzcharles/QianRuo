package com.ruoyi.system.enums;

/** 平台知识文章状态。 */
public enum PlatformKnowledgeStatus
{
    DRAFT(0, "草稿"), PUBLISHED(1, "已发布"), OFFLINE(2, "已下架");

    private final int code;
    private final String label;

    PlatformKnowledgeStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static PlatformKnowledgeStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (PlatformKnowledgeStatus value : values())
            {
                if (value.code == code)
                    return value;
            }
        }
        return null;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
}
