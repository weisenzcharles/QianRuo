package com.ruoyi.merchant.model;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户财务页面流水查询条件。归属范围由当前登录商户上下文决定。 */
public class MerchantFinanceLedgerQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String ledgerNo;
    private Integer accountType;
    private Integer direction;
    private String bizType;
    private Integer ledgerStatus;
    private Integer invoiceStatus;
    private String beginTime;
    private String endTime;
    private String scene;

    public String getLedgerNo() { return ledgerNo; }
    public void setLedgerNo(String ledgerNo) { this.ledgerNo = ledgerNo; }
    public Integer getAccountType() { return accountType; }
    public void setAccountType(Integer accountType) { this.accountType = accountType; }
    public Integer getDirection() { return direction; }
    public void setDirection(Integer direction) { this.direction = direction; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Integer getLedgerStatus() { return ledgerStatus; }
    public void setLedgerStatus(Integer ledgerStatus) { this.ledgerStatus = ledgerStatus; }
    public Integer getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(Integer invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
}
