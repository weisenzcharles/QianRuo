package com.ruoyi.rentalbusiness.enums;

/** 外部订单回调出站状态。数据库只保存受控数值编码。 */
public enum MerchantExternalCallbackEventStatus
{
    PENDING(0, "待发送"),
    SENDING(1, "发送中"),
    DELIVERED(2, "已送达"),
    RETRYING(3, "待重试"),
    EXHAUSTED(4, "重试耗尽");

    private final int code;
    private final String label;

    MerchantExternalCallbackEventStatus(int code, String label)
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
