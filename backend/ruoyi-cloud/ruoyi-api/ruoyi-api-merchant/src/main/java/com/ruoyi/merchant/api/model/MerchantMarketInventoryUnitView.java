package com.ruoyi.merchant.api.model;

import java.io.Serializable;

/** 周转市场可绑定逐件库存的内部视图。 */
public class MerchantMarketInventoryUnitView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long unitId;
    private String unitCode;
    private String serialNumber;
    private String brand;
    private String modelName;
    private String memorySpec;
    private Integer conditionLevel;
    private String warehouseName;

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getMemorySpec() { return memorySpec; }
    public void setMemorySpec(String memorySpec) { this.memorySpec = memorySpec; }
    public Integer getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(Integer conditionLevel) { this.conditionLevel = conditionLevel; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
}
