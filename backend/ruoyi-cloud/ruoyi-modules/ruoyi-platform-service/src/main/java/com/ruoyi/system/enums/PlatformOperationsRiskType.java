package com.ruoyi.system.enums;

/** 平台运营风险类型。 */
public enum PlatformOperationsRiskType
{
    INVENTORY(1, "库存"),
    ORDER(2, "订单"),
    FINANCE(3, "财务"),
    WORK_ORDER(4, "工单"),
    SYSTEM(5, "系统");

    private final int code;
    private final String label;

    PlatformOperationsRiskType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
}
