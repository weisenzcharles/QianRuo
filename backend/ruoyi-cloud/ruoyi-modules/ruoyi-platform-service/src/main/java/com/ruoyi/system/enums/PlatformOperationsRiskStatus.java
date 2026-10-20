package com.ruoyi.system.enums;

/** 平台运营风险事件状态。 */
public enum PlatformOperationsRiskStatus
{
    PENDING(0, "待处理"),
    ACKNOWLEDGED(1, "已确认"),
    RESOLVED(2, "已解除");

    private final int code;
    private final String label;

    PlatformOperationsRiskStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformOperationsRiskStatus fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformOperationsRiskStatus item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
