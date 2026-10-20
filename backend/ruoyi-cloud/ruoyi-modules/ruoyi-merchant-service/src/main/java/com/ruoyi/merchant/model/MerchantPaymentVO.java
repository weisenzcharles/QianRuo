package com.ruoyi.merchant.model;

import java.math.BigDecimal;

/** 商户支付单浏览器响应，只返回业务编码和支付表单，不暴露数据库唯一编号。 */
public class MerchantPaymentVO
{
    private String paymentNo;
    private String bizType;
    private String bizTypeName;
    private String bizNo;
    private String subject;
    private BigDecimal amount;
    private Integer paymentStatus;
    private String paymentStatusName;
    private String paymentForm;
    private String paidTime;
    private String expireTime;

    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizTypeName() { return bizTypeName; }
    public void setBizTypeName(String bizTypeName) { this.bizTypeName = bizTypeName; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentStatusName() { return paymentStatusName; }
    public void setPaymentStatusName(String paymentStatusName) { this.paymentStatusName = paymentStatusName; }
    public String getPaymentForm() { return paymentForm; }
    public void setPaymentForm(String paymentForm) { this.paymentForm = paymentForm; }
    public String getPaidTime() { return paidTime; }
    public void setPaidTime(String paidTime) { this.paidTime = paidTime; }
    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }
}
