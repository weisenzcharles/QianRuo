package com.ruoyi.system.api.model;

import java.io.Serializable;

/**
 * 租赁业务服务返回的平台商户经营统计。
 *
 * <p>该对象只用于平台服务与 rental-business 的内部调用，memberId 仅用于两边关联，
 * 平台浏览器接口不会直接返回该字段。</p>
 */
public class PlatformMerchantOverviewStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 商户唯一编号，仅用于内部关联。 */
    private Long memberId;
    private long warehouseCount;
    private long deviceProductCount;
    private long accessoryCount;
    private long packageCount;
    private long totalInventoryQuantity;
    private long availableInventoryQuantity;
    private long reservedInventoryQuantity;
    private long rentedInventoryQuantity;
    private long repairingInventoryQuantity;
    private long damagedInventoryQuantity;
    private long pendingOrderCount;
    private long activeOrderCount;
    private long completedOrderCount;
    private long overdueOrderCount;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public long getWarehouseCount() { return warehouseCount; }
    public void setWarehouseCount(long warehouseCount) { this.warehouseCount = warehouseCount; }
    public long getDeviceProductCount() { return deviceProductCount; }
    public void setDeviceProductCount(long deviceProductCount) { this.deviceProductCount = deviceProductCount; }
    public long getAccessoryCount() { return accessoryCount; }
    public void setAccessoryCount(long accessoryCount) { this.accessoryCount = accessoryCount; }
    public long getPackageCount() { return packageCount; }
    public void setPackageCount(long packageCount) { this.packageCount = packageCount; }
    public long getTotalInventoryQuantity() { return totalInventoryQuantity; }
    public void setTotalInventoryQuantity(long totalInventoryQuantity) { this.totalInventoryQuantity = totalInventoryQuantity; }
    public long getAvailableInventoryQuantity() { return availableInventoryQuantity; }
    public void setAvailableInventoryQuantity(long availableInventoryQuantity) { this.availableInventoryQuantity = availableInventoryQuantity; }
    public long getReservedInventoryQuantity() { return reservedInventoryQuantity; }
    public void setReservedInventoryQuantity(long reservedInventoryQuantity) { this.reservedInventoryQuantity = reservedInventoryQuantity; }
    public long getRentedInventoryQuantity() { return rentedInventoryQuantity; }
    public void setRentedInventoryQuantity(long rentedInventoryQuantity) { this.rentedInventoryQuantity = rentedInventoryQuantity; }
    public long getRepairingInventoryQuantity() { return repairingInventoryQuantity; }
    public void setRepairingInventoryQuantity(long repairingInventoryQuantity) { this.repairingInventoryQuantity = repairingInventoryQuantity; }
    public long getDamagedInventoryQuantity() { return damagedInventoryQuantity; }
    public void setDamagedInventoryQuantity(long damagedInventoryQuantity) { this.damagedInventoryQuantity = damagedInventoryQuantity; }
    public long getPendingOrderCount() { return pendingOrderCount; }
    public void setPendingOrderCount(long pendingOrderCount) { this.pendingOrderCount = pendingOrderCount; }
    public long getActiveOrderCount() { return activeOrderCount; }
    public void setActiveOrderCount(long activeOrderCount) { this.activeOrderCount = activeOrderCount; }
    public long getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(long completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public long getOverdueOrderCount() { return overdueOrderCount; }
    public void setOverdueOrderCount(long overdueOrderCount) { this.overdueOrderCount = overdueOrderCount; }
}
