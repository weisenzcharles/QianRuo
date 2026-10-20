package com.ruoyi.rentalbusiness.enums;

/** 外部订单回调事件类型。数据库只保存受控数值编码。 */
public enum MerchantExternalCallbackEventType
{
    ORDER_CREATED(1, "订单已创建"),
    ORDER_STATUS_CHANGED(2, "订单状态已变更");

    private final int code;
    private final String label;

    MerchantExternalCallbackEventType(int code, String label)
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
}
