package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台查看商户仓库的只读视图，不包含仓库技术主键。 */
public class PlatformMerchantWarehouseView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String warehouseCode;
    private String warehouseName;
    private String city;
    private Integer returnEnabled;
    private Integer allianceShareEnabled;
    private Integer status;

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Integer getReturnEnabled() { return returnEnabled; }
    public void setReturnEnabled(Integer returnEnabled) { this.returnEnabled = returnEnabled; }
    public Integer getAllianceShareEnabled() { return allianceShareEnabled; }
    public void setAllianceShareEnabled(Integer allianceShareEnabled) { this.allianceShareEnabled = allianceShareEnabled; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
