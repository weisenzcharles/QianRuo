package com.ruoyi.system.enums;

/** 平台通用工作台待办类型。 */
public enum PlatformWorkbenchTaskType
{
    RISK(1, "运营风险"), TICKET(2, "客服工单"), WITHDRAWAL(3, "提现审核"), INVOICE(4, "发票审核"),
    MARKET_REPORT(5, "市场举报"), FULFILLMENT(6, "订单履约"), AFTER_SALES(7, "售后纠纷"),
    LOGISTICS(8, "物流异常"), INVENTORY_ALERT(9, "库存告警");

    private final int code;
    private final String label;

    PlatformWorkbenchTaskType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformWorkbenchTaskType fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformWorkbenchTaskType item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
