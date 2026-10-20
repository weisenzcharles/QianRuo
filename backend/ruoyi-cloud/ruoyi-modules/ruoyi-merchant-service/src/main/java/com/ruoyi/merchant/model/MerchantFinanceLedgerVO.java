package com.ruoyi.merchant.model;

import java.math.BigDecimal;

/** 商户财务流水展示对象，只返回页面需要的业务编码和中文状态。 */
public class MerchantFinanceLedgerVO
{
    private String ledgerNo;
    private Integer accountType;
    private String accountTypeName;
    private Integer direction;
    private String directionName;
    private String bizType;
    private String bizTypeName;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String refType;
    private String referenceNo;
    private String payChannel;
    private String payOrderNo;
    private Integer invoiceStatus;
    private String invoiceStatusName;
    private Integer ledgerStatus;
    private String ledgerStatusName;
    private String originalLedgerNo;
    private String createTime;
    private String remark;

    public String getLedgerNo() { return ledgerNo; }
    public void setLedgerNo(String ledgerNo) { this.ledgerNo = ledgerNo; }
    public Integer getAccountType() { return accountType; }
    public void setAccountType(Integer accountType) { this.accountType = accountType; }
    public String getAccountTypeName() { return accountTypeName; }
    public void setAccountTypeName(String accountTypeName) { this.accountTypeName = accountTypeName; }
    public Integer getDirection() { return direction; }
    public void setDirection(Integer direction) { this.direction = direction; }
    public String getDirectionName() { return directionName; }
    public void setDirectionName(String directionName) { this.directionName = directionName; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizTypeName() { return bizTypeName; }
    public void setBizTypeName(String bizTypeName) { this.bizTypeName = bizTypeName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getPayChannel() { return payChannel; }
    public void setPayChannel(String payChannel) { this.payChannel = payChannel; }
    public String getPayOrderNo() { return payOrderNo; }
    public void setPayOrderNo(String payOrderNo) { this.payOrderNo = payOrderNo; }
    public Integer getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(Integer invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public String getInvoiceStatusName() { return invoiceStatusName; }
    public void setInvoiceStatusName(String invoiceStatusName) { this.invoiceStatusName = invoiceStatusName; }
    public Integer getLedgerStatus() { return ledgerStatus; }
    public void setLedgerStatus(Integer ledgerStatus) { this.ledgerStatus = ledgerStatus; }
    public String getLedgerStatusName() { return ledgerStatusName; }
    public void setLedgerStatusName(String ledgerStatusName) { this.ledgerStatusName = ledgerStatusName; }
    public String getOriginalLedgerNo() { return originalLedgerNo; }
    public void setOriginalLedgerNo(String originalLedgerNo) { this.originalLedgerNo = originalLedgerNo; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
