package com.ruoyi.merchant.domain;

/** 商户仓库摘要。 */
public class MerchantWarehouse
{
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Integer status;

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
