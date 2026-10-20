package com.ruoyi.rentalbusiness.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 库存预测快照持久化记录。 */
public class MerchantInventoryForecastRecord
{
    public Long forecastId;
    public Long tenantId;
    public Long memberId;
    public Long warehouseId;
    public Integer forecastType;
    public Long itemId;
    public String itemCode;
    public String itemName;
    public String brand;
    public LocalDate snapshotDate;
    public Integer totalQuantity;
    public Integer occupiedQuantity;
    public Integer availableQuantity;
    public Integer orderableQuantity;
    public Integer riskLevel;
    public String riskMessage;
    public LocalDateTime generatedTime;
    public Long createBy;
    public LocalDateTime createTime;
    public Long updateBy;
    public LocalDateTime updateTime;
    public Long removeBy;
    public LocalDateTime removeTime;
    public Integer isDelete;
}
