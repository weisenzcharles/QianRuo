package com.ruoyi.merchant.model;

import java.math.BigDecimal;

/** 商户开票申请参数。流水归属和金额由服务端按流水号重新读取。 */
public class MerchantInvoiceApplicationCreateCommand
{
    private String ledgerNo;
    private Integer applicationType = MerchantInvoiceApplicationType.ISSUE.getCode();
    private Integer invoiceType = 1;
    private String invoiceTitle;
    private String taxNo;
    private String invoiceEmail;
    private String idempotencyKey;
    private BigDecimal amount;

    public String getLedgerNo() { return ledgerNo; }
    public void setLedgerNo(String ledgerNo) { this.ledgerNo = ledgerNo; }
    public Integer getApplicationType() { return applicationType; }
    public void setApplicationType(Integer applicationType) { this.applicationType = applicationType; }
    public Integer getInvoiceType() { return invoiceType; }
    public void setInvoiceType(Integer invoiceType) { this.invoiceType = invoiceType; }
    public String getInvoiceTitle() { return invoiceTitle; }
    public void setInvoiceTitle(String invoiceTitle) { this.invoiceTitle = invoiceTitle; }
    public String getTaxNo() { return taxNo; }
    public void setTaxNo(String taxNo) { this.taxNo = taxNo; }
    public String getInvoiceEmail() { return invoiceEmail; }
    public void setInvoiceEmail(String invoiceEmail) { this.invoiceEmail = invoiceEmail; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
