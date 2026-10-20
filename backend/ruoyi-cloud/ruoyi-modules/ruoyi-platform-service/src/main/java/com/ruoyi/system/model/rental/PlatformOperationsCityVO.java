package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台经营分析城市报表行，只返回城市名称和统计指标。 */
public class PlatformOperationsCityVO
{
    private String cityName;
    private long orderCount;
    private BigDecimal rentAmount = BigDecimal.ZERO;
    private long completedOrderCount;
    private long overdueOrderCount;

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    public BigDecimal getRentAmount() { return rentAmount; }
    public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
    public long getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(long completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public long getOverdueOrderCount() { return overdueOrderCount; }
    public void setOverdueOrderCount(long overdueOrderCount) { this.overdueOrderCount = overdueOrderCount; }
}
