package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台财务汇总展示对象。所有金额均由财务账户和不可变流水聚合。 */
public class PlatformFinanceSummaryVO
{
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;
    private BigDecimal frozenAmount = BigDecimal.ZERO;
    private BigDecimal monthIncome = BigDecimal.ZERO;
    private BigDecimal monthExpense = BigDecimal.ZERO;
    private Integer pendingInvoiceCount = 0;
    private Integer abnormalLedgerCount = 0;
    private Integer merchantCount = 0;
    private Integer paymentOrderCount = 0;
    private Integer unpaidPaymentCount = 0;
    private BigDecimal paidPaymentAmount = BigDecimal.ZERO;
    private BigDecimal unpaidPaymentAmount = BigDecimal.ZERO;

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getFrozenAmount() { return frozenAmount; }
    public void setFrozenAmount(BigDecimal frozenAmount) { this.frozenAmount = frozenAmount; }
    public BigDecimal getMonthIncome() { return monthIncome; }
    public void setMonthIncome(BigDecimal monthIncome) { this.monthIncome = monthIncome; }
    public BigDecimal getMonthExpense() { return monthExpense; }
    public void setMonthExpense(BigDecimal monthExpense) { this.monthExpense = monthExpense; }
    public Integer getPendingInvoiceCount() { return pendingInvoiceCount; }
    public void setPendingInvoiceCount(Integer pendingInvoiceCount) { this.pendingInvoiceCount = pendingInvoiceCount; }
    public Integer getAbnormalLedgerCount() { return abnormalLedgerCount; }
    public void setAbnormalLedgerCount(Integer abnormalLedgerCount) { this.abnormalLedgerCount = abnormalLedgerCount; }
    public Integer getMerchantCount() { return merchantCount; }
    public void setMerchantCount(Integer merchantCount) { this.merchantCount = merchantCount; }
    public Integer getPaymentOrderCount() { return paymentOrderCount; }
    public void setPaymentOrderCount(Integer paymentOrderCount) { this.paymentOrderCount = paymentOrderCount; }
    public Integer getUnpaidPaymentCount() { return unpaidPaymentCount; }
    public void setUnpaidPaymentCount(Integer unpaidPaymentCount) { this.unpaidPaymentCount = unpaidPaymentCount; }
    public BigDecimal getPaidPaymentAmount() { return paidPaymentAmount; }
    public void setPaidPaymentAmount(BigDecimal paidPaymentAmount) { this.paidPaymentAmount = paidPaymentAmount; }
    public BigDecimal getUnpaidPaymentAmount() { return unpaidPaymentAmount; }
    public void setUnpaidPaymentAmount(BigDecimal unpaidPaymentAmount) { this.unpaidPaymentAmount = unpaidPaymentAmount; }
}
