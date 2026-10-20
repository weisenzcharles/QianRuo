package com.ruoyi.merchant.model;

import java.math.BigDecimal;

/** 商户财务中心展示的开票申请及已开具发票信息。 */
public class MerchantInvoiceApplicationVO
{
    private String applicationNo;
    private Integer applicationType;
    private String applicationTypeName;
    private Integer applicationStatus;
    private String applicationStatusName;
    private String ledgerNo;
    private BigDecimal amount;
    private Integer invoiceType;
    private String invoiceTitle;
    private String taxNo;
    private String invoiceEmail;
    private String originalApplicationNo;
    private String invoiceNo;
    private String invoiceCode;
    private String invoiceFileUrl;
    private String reviewRemark;
    private String reviewTime;
    private String createTime;

    public String getApplicationNo() { return applicationNo; }
    public void setApplicationNo(String applicationNo) { this.applicationNo = applicationNo; }
    public Integer getApplicationType() { return applicationType; }
    public void setApplicationType(Integer applicationType) { this.applicationType = applicationType; }
    public String getApplicationTypeName() { return applicationTypeName; }
    public void setApplicationTypeName(String applicationTypeName) { this.applicationTypeName = applicationTypeName; }
    public Integer getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(Integer applicationStatus) { this.applicationStatus = applicationStatus; }
    public String getApplicationStatusName() { return applicationStatusName; }
    public void setApplicationStatusName(String applicationStatusName) { this.applicationStatusName = applicationStatusName; }
    public String getLedgerNo() { return ledgerNo; }
    public void setLedgerNo(String ledgerNo) { this.ledgerNo = ledgerNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getInvoiceType() { return invoiceType; }
    public void setInvoiceType(Integer invoiceType) { this.invoiceType = invoiceType; }
    public String getInvoiceTitle() { return invoiceTitle; }
    public void setInvoiceTitle(String invoiceTitle) { this.invoiceTitle = invoiceTitle; }
    public String getTaxNo() { return taxNo; }
    public void setTaxNo(String taxNo) { this.taxNo = taxNo; }
    public String getInvoiceEmail() { return invoiceEmail; }
    public void setInvoiceEmail(String invoiceEmail) { this.invoiceEmail = invoiceEmail; }
    public String getOriginalApplicationNo() { return originalApplicationNo; }
    public void setOriginalApplicationNo(String originalApplicationNo) { this.originalApplicationNo = originalApplicationNo; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getInvoiceFileUrl() { return invoiceFileUrl; }
    public void setInvoiceFileUrl(String invoiceFileUrl) { this.invoiceFileUrl = invoiceFileUrl; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public String getReviewTime() { return reviewTime; }
    public void setReviewTime(String reviewTime) { this.reviewTime = reviewTime; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
