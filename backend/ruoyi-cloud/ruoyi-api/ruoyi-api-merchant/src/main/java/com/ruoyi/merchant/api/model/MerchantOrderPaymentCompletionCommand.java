package com.ruoyi.merchant.api.model;

import java.io.Serializable;

/** 支付宝订单支付成功后通知租赁业务服务更新订单付款状态。 */
public class MerchantOrderPaymentCompletionCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private Long memberId;
    private Long orderId;
    private String paymentNo;
    private String alipayTradeNo;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getAlipayTradeNo() { return alipayTradeNo; }
    public void setAlipayTradeNo(String alipayTradeNo) { this.alipayTradeNo = alipayTradeNo; }
}
