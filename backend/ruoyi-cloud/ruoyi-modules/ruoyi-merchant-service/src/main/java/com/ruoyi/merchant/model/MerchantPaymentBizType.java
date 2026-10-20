package com.ruoyi.merchant.model;

/** 商户支付宝支付业务类型。数据库保存稳定编码，页面只展示中文名称。 */
public enum MerchantPaymentBizType
{
    RECHARGE("recharge", "账户充值"),
    ORDER("order", "订单支付");

    private final String code;
    private final String label;

    MerchantPaymentBizType(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static MerchantPaymentBizType fromCode(String code)
    {
        for (MerchantPaymentBizType item : values())
        {
            if (item.code.equalsIgnoreCase(code)) return item;
        }
        return null;
    }
}
