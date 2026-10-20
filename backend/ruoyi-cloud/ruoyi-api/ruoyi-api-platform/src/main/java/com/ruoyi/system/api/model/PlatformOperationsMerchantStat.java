package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台经营分析的商户聚合指标，memberId 仅用于跨服务内部关联。 */
public class PlatformOperationsMerchantStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private long orderCount;
    private BigDecimal rentAmount = BigDecimal.ZERO;
    private long activeOrderCount;
    private long completedOrderCount;
    private long overdueOrderCount;
    private long totalInventoryQuantity;
    private long availableInventoryQuantity;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
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
}
