package com.ruoyi.system.enums;

/** 平台运营风险等级。 */
public enum PlatformOperationsRiskLevel
{
    NORMAL(0, "正常"),
    NOTICE(1, "提示"),
    WARNING(2, "预警"),
    HIGH(3, "高风险");

    private final int code;
    private final String label;

    PlatformOperationsRiskLevel(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
}
