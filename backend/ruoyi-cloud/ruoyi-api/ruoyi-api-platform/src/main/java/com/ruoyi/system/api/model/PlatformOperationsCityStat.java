package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台经营分析的城市聚合指标，供平台服务内部组装使用。 */
public class PlatformOperationsCityStat implements Serializable
{
    private static final long serialVersionUID = 1L;

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
