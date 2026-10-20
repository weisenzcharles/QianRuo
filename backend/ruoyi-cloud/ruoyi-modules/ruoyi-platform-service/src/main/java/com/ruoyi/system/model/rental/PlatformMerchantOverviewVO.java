package com.ruoyi.system.model.rental;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 平台端商户经营概况展示对象，不包含商户或租户技术主键。 */
public class PlatformMerchantOverviewVO
{
    private String merchantName;
    private String legalSubjectName;
    private String memberCode;
    private Integer authStatus;
    private Integer status;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getLegalSubjectName() { return legalSubjectName; }
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public Integer getAuthStatus() { return authStatus; }
    public void setAuthStatus(Integer authStatus) { this.authStatus = authStatus; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
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
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
