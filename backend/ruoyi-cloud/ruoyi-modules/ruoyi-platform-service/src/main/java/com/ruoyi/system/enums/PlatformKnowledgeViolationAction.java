package com.ruoyi.system.enums;

/** 平台知识违规处置动作。 */
public enum PlatformKnowledgeViolationAction
{
    OFFLINE(1, "违规下架");

    private final int code;
    private final String label;

    PlatformKnowledgeViolationAction(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
}
