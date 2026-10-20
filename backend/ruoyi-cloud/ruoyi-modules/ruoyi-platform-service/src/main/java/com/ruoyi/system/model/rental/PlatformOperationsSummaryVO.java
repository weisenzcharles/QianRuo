package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台经营分析周期汇总。 */
public class PlatformOperationsSummaryVO
{
    private int periodDays;
    private long merchantCount;
    private long orderCount;
    private BigDecimal rentAmount = BigDecimal.ZERO;
    private long activeOrderCount;
    private long completedOrderCount;
    private long overdueOrderCount;
    private long totalInventoryQuantity;
    private long availableInventoryQuantity;
    private long lowInventoryMerchantCount;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;
    private int pendingTicketCount;
    private int pendingWithdrawalCount;
    private int abnormalLedgerCount;

    public int getPeriodDays() { return periodDays; }
    public void setPeriodDays(int periodDays) { this.periodDays = periodDays; }
    public long getMerchantCount() { return merchantCount; }
    public void setMerchantCount(long merchantCount) { this.merchantCount = merchantCount; }
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    public BigDecimal getRentAmount() { return rentAmount; }
    public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
    public long getActiveOrderCount() { return activeOrderCount; }
    public void setActiveOrderCount(long activeOrderCount) { this.activeOrderCount = activeOrderCount; }
    public long getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(long completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public long getOverdueOrderCount() { return overdueOrderCount; }
    public void setOverdueOrderCount(long overdueOrderCount) { this.overdueOrderCount = overdueOrderCount; }
    public long getTotalInventoryQuantity() { return totalInventoryQuantity; }
    public void setTotalInventoryQuantity(long totalInventoryQuantity) { this.totalInventoryQuantity = totalInventoryQuantity; }
    public long getAvailableInventoryQuantity() { return availableInventoryQuantity; }
    public void setAvailableInventoryQuantity(long availableInventoryQuantity) { this.availableInventoryQuantity = availableInventoryQuantity; }
    public long getLowInventoryMerchantCount() { return lowInventoryMerchantCount; }
    public void setLowInventoryMerchantCount(long lowInventoryMerchantCount) { this.lowInventoryMerchantCount = lowInventoryMerchantCount; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public int getPendingTicketCount() { return pendingTicketCount; }
    public void setPendingTicketCount(int pendingTicketCount) { this.pendingTicketCount = pendingTicketCount; }
    public int getPendingWithdrawalCount() { return pendingWithdrawalCount; }
    public void setPendingWithdrawalCount(int pendingWithdrawalCount) { this.pendingWithdrawalCount = pendingWithdrawalCount; }
    public int getAbnormalLedgerCount() { return abnormalLedgerCount; }
    public void setAbnormalLedgerCount(int abnormalLedgerCount) { this.abnormalLedgerCount = abnormalLedgerCount; }
}
