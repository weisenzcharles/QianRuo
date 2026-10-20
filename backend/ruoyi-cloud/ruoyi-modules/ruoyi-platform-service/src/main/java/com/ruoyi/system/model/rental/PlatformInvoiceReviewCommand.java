package com.ruoyi.system.model.rental;

/** 平台审核发票申请参数。通过时可直接填写已开具发票信息，否则进入待开具。 */
public class PlatformInvoiceReviewCommand
{
    private Boolean approved;
    private String reviewRemark;
    private String invoiceNo;
    private String invoiceCode;
    private String invoiceFileUrl;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getInvoiceFileUrl() { return invoiceFileUrl; }
    public void setInvoiceFileUrl(String invoiceFileUrl) { this.invoiceFileUrl = invoiceFileUrl; }
}
