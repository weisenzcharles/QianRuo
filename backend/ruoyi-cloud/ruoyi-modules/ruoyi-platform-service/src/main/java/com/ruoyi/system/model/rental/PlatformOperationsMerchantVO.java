package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台经营分析商户排行，浏览器接口只返回业务编码和名称。 */
public class PlatformOperationsMerchantVO
{
    private String memberCode;
    private String merchantName;
    private long orderCount;
    private BigDecimal rentAmount = BigDecimal.ZERO;
    private long activeOrderCount;
    private long completedOrderCount;
    private long overdueOrderCount;
    private long totalInventoryQuantity;
    private long availableInventoryQuantity;

    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
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
