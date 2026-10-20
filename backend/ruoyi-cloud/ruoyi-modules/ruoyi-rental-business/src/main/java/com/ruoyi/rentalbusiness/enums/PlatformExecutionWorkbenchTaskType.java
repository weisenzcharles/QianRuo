package com.ruoyi.rentalbusiness.enums;

/** 平台执行面待办类型，与平台工作台跨服务契约保持数值映射。 */
public enum PlatformExecutionWorkbenchTaskType
{
    FULFILLMENT(6, "订单履约"),
    AFTER_SALES(7, "售后纠纷"),
    LOGISTICS(8, "物流异常"),
    INVENTORY_ALERT(9, "库存告警");

    private final Integer code;
    private final String label;

    PlatformExecutionWorkbenchTaskType(Integer code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public Integer getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }
}
