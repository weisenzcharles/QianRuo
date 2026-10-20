package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台经营分析每日趋势。 */
public class PlatformOperationsTrendVO
{
    private String statDate;
    private long orderCount;
    private BigDecimal rentAmount = BigDecimal.ZERO;
    private long completedOrderCount;
    private long overdueOrderCount;

    public String getStatDate() { return statDate; }
    public void setStatDate(String statDate) { this.statDate = statDate; }
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    public BigDecimal getRentAmount() { return rentAmount; }
    public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
    public long getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(long completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public long getOverdueOrderCount() { return overdueOrderCount; }
    public void setOverdueOrderCount(long overdueOrderCount) { this.overdueOrderCount = overdueOrderCount; }
}
