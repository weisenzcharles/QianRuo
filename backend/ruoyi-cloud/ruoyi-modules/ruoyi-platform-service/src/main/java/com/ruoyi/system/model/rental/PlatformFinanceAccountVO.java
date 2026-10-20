package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台按商户汇总的财务账户展示对象。 */
public class PlatformFinanceAccountVO
{
    private String memberCode;
    private String merchantName;
    private String legalSubjectName;
    private BigDecimal availableAmount = BigDecimal.ZERO;
    private BigDecimal frozenAmount = BigDecimal.ZERO;
    private BigDecimal valueAddedAvailableAmount = BigDecimal.ZERO;
    private BigDecimal performanceDepositAvailableAmount = BigDecimal.ZERO;
    private BigDecimal performanceDepositFrozenAmount = BigDecimal.ZERO;
    private BigDecimal rentalSettlementAvailableAmount = BigDecimal.ZERO;
    private BigDecimal rentalSettlementFrozenAmount = BigDecimal.ZERO;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private Integer pendingInvoiceCount = 0;
    private Integer abnormalLedgerCount = 0;

    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getLegalSubjectName() { return legalSubjectName; }
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }
    public BigDecimal getAvailableAmount() { return availableAmount; }
    public void setAvailableAmount(BigDecimal availableAmount) { this.availableAmount = availableAmount; }
    public BigDecimal getFrozenAmount() { return frozenAmount; }
    public void setFrozenAmount(BigDecimal frozenAmount) { this.frozenAmount = frozenAmount; }
    public BigDecimal getValueAddedAvailableAmount() { return valueAddedAvailableAmount; }
    public void setValueAddedAvailableAmount(BigDecimal valueAddedAvailableAmount) { this.valueAddedAvailableAmount = valueAddedAvailableAmount; }
    public BigDecimal getPerformanceDepositAvailableAmount() { return performanceDepositAvailableAmount; }
    public void setPerformanceDepositAvailableAmount(BigDecimal performanceDepositAvailableAmount) { this.performanceDepositAvailableAmount = performanceDepositAvailableAmount; }
    public BigDecimal getPerformanceDepositFrozenAmount() { return performanceDepositFrozenAmount; }
    public void setPerformanceDepositFrozenAmount(BigDecimal performanceDepositFrozenAmount) { this.performanceDepositFrozenAmount = performanceDepositFrozenAmount; }
    public BigDecimal getRentalSettlementAvailableAmount() { return rentalSettlementAvailableAmount; }
    public void setRentalSettlementAvailableAmount(BigDecimal rentalSettlementAvailableAmount) { this.rentalSettlementAvailableAmount = rentalSettlementAvailableAmount; }
    public BigDecimal getRentalSettlementFrozenAmount() { return rentalSettlementFrozenAmount; }
    public void setRentalSettlementFrozenAmount(BigDecimal rentalSettlementFrozenAmount) { this.rentalSettlementFrozenAmount = rentalSettlementFrozenAmount; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public Integer getPendingInvoiceCount() { return pendingInvoiceCount; }
    public void setPendingInvoiceCount(Integer pendingInvoiceCount) { this.pendingInvoiceCount = pendingInvoiceCount; }
    public Integer getAbnormalLedgerCount() { return abnormalLedgerCount; }
    public void setAbnormalLedgerCount(Integer abnormalLedgerCount) { this.abnormalLedgerCount = abnormalLedgerCount; }
}
