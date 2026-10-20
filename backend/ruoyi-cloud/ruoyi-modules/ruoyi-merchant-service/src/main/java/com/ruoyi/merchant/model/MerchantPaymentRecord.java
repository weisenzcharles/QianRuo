package com.ruoyi.merchant.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付单内部记录，不直接作为浏览器接口响应。 */
public class MerchantPaymentRecord
{
    private Long paymentId;
    private Long tenantId;
    private Long memberId;
    private String paymentNo;
    private String bizType;
    private Long bizId;
    private String bizNo;
    private String subject;
    private BigDecimal amount;
    private Integer paymentStatus;
    private String alipayTradeNo;
    private String notifyId;
    private LocalDateTime paidTime;
    private LocalDateTime expireTime;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getAlipayTradeNo() { return alipayTradeNo; }
    public void setAlipayTradeNo(String alipayTradeNo) { this.alipayTradeNo = alipayTradeNo; }
    public String getNotifyId() { return notifyId; }
    public void setNotifyId(String notifyId) { this.notifyId = notifyId; }
    public LocalDateTime getPaidTime() { return paidTime; }
    public void setPaidTime(LocalDateTime paidTime) { this.paidTime = paidTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
}
