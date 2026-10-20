package com.ruoyi.merchant.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 租赁业务服务返回的订单支付目标，供商户支付服务创建支付宝支付单。 */
public class MerchantOrderPaymentTarget implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNo;
    private BigDecimal amount;
    private Integer payStatus;
    private Integer orderStatus;
    private String subject;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
