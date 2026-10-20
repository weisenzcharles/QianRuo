package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台查看商户库存汇总的只读视图，不包含库存、商品或仓库技术主键。 */
public class PlatformMerchantInventoryView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String warehouseName;
    private String productCode;
    private String productName;
    private String productType;
    private long totalQuantity;
    private long availableQuantity;
    private long reservedQuantity;
    private long rentedQuantity;
    private long repairingQuantity;
    private long damagedQuantity;
    private Integer status;

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public long getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(long totalQuantity) { this.totalQuantity = totalQuantity; }
    public long getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(long availableQuantity) { this.availableQuantity = availableQuantity; }
    public long getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(long reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public long getRentedQuantity() { return rentedQuantity; }
    public void setRentedQuantity(long rentedQuantity) { this.rentedQuantity = rentedQuantity; }
    public long getRepairingQuantity() { return repairingQuantity; }
    public void setRepairingQuantity(long repairingQuantity) { this.repairingQuantity = repairingQuantity; }
    public long getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(long damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
