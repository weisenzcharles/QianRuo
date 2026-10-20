package com.ruoyi.merchant.model;

import java.math.BigDecimal;
import java.util.List;

/** 商户财务总览展示对象。 */
public class MerchantFinanceOverviewVO
{
    private BigDecimal availableBalance = BigDecimal.ZERO;
    private BigDecimal valueAddedBalance = BigDecimal.ZERO;
    private BigDecimal rentalSettlementBalance = BigDecimal.ZERO;
    private BigDecimal performanceDepositAvailable = BigDecimal.ZERO;
    private BigDecimal performanceDepositFrozen = BigDecimal.ZERO;
    private BigDecimal monthIncome = BigDecimal.ZERO;
    private BigDecimal monthExpense = BigDecimal.ZERO;
    private BigDecimal frozenAmount = BigDecimal.ZERO;
    private BigDecimal pendingSettlementAmount = BigDecimal.ZERO;
    private Integer pendingInvoiceCount = 0;
    private Integer pendingWithdrawalCount = 0;
    private Integer abnormalLedgerCount = 0;
    private List<MerchantFinanceLedgerVO> recentLedgers;

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
    public BigDecimal getValueAddedBalance() { return valueAddedBalance; }
    public void setValueAddedBalance(BigDecimal valueAddedBalance) { this.valueAddedBalance = valueAddedBalance; }
    public BigDecimal getRentalSettlementBalance() { return rentalSettlementBalance; }
    public void setRentalSettlementBalance(BigDecimal rentalSettlementBalance) { this.rentalSettlementBalance = rentalSettlementBalance; }
    public BigDecimal getPerformanceDepositAvailable() { return performanceDepositAvailable; }
    public void setPerformanceDepositAvailable(BigDecimal performanceDepositAvailable) { this.performanceDepositAvailable = performanceDepositAvailable; }
    public BigDecimal getPerformanceDepositFrozen() { return performanceDepositFrozen; }
    public void setPerformanceDepositFrozen(BigDecimal performanceDepositFrozen) { this.performanceDepositFrozen = performanceDepositFrozen; }
    public BigDecimal getMonthIncome() { return monthIncome; }
    public void setMonthIncome(BigDecimal monthIncome) { this.monthIncome = monthIncome; }
    public BigDecimal getMonthExpense() { return monthExpense; }
    public void setMonthExpense(BigDecimal monthExpense) { this.monthExpense = monthExpense; }
    public BigDecimal getFrozenAmount() { return frozenAmount; }
    public void setFrozenAmount(BigDecimal frozenAmount) { this.frozenAmount = frozenAmount; }
    public BigDecimal getPendingSettlementAmount() { return pendingSettlementAmount; }
    public void setPendingSettlementAmount(BigDecimal pendingSettlementAmount) { this.pendingSettlementAmount = pendingSettlementAmount; }
    public Integer getPendingInvoiceCount() { return pendingInvoiceCount; }
    public void setPendingInvoiceCount(Integer pendingInvoiceCount) { this.pendingInvoiceCount = pendingInvoiceCount; }
    public Integer getPendingWithdrawalCount() { return pendingWithdrawalCount; }
    public void setPendingWithdrawalCount(Integer pendingWithdrawalCount) { this.pendingWithdrawalCount = pendingWithdrawalCount; }
    public Integer getAbnormalLedgerCount() { return abnormalLedgerCount; }
    public void setAbnormalLedgerCount(Integer abnormalLedgerCount) { this.abnormalLedgerCount = abnormalLedgerCount; }
    public List<MerchantFinanceLedgerVO> getRecentLedgers() { return recentLedgers; }
    public void setRecentLedgers(List<MerchantFinanceLedgerVO> recentLedgers) { this.recentLedgers = recentLedgers; }
}
