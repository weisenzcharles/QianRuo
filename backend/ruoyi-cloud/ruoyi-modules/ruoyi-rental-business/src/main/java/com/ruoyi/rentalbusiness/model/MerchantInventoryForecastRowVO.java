package com.ruoyi.rentalbusiness.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** 商户库存预测页面行，隐藏数据库内部唯一编号，仅保留筛选所需的业务数据。 */
public class MerchantInventoryForecastRowVO
{
    @JsonIgnore
    public Long forecastId;
    @JsonIgnore
    public Long warehouseId;
    @JsonIgnore
    public Long itemId;
    public Integer forecastType;
    public String forecastTypeName;
    public String itemCode;
    public String itemName;
    public String brand;
    public LocalDate snapshotDate;
    public Integer totalQuantity;
    public Integer occupiedQuantity;
    public Integer availableQuantity;
    public Integer orderableQuantity;
    public Integer riskLevel;
    public String riskLevelName;
    public String riskMessage;
    public LocalDateTime generatedTime;
}
