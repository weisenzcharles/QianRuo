package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台支付订单展示对象。 */
public class PlatformPaymentOrderVO
{
    private String paymentNo;
    private String memberCode;
    private String merchantName;
    private String bizType;
    private String bizTypeName;
    private String bizNo;
    private String subject;
    private BigDecimal amount;
    private Integer paymentStatus;
    private String paymentStatusName;
    private String alipayTradeNo;
    private String createTime;
    private String expireTime;
    private String paidTime;
    private String remark;

    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
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
    public String getAlipayTradeNo() { return alipayTradeNo; }
    public void setAlipayTradeNo(String alipayTradeNo) { this.alipayTradeNo = alipayTradeNo; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }
    public String getPaidTime() { return paidTime; }
    public void setPaidTime(String paidTime) { this.paidTime = paidTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
