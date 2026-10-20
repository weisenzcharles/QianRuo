package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryCalculationMode;

/** 修改库存可接单口径命令。 */
public class MerchantInventoryPolicyCommand
{
    public Long inventoryId;
    public MerchantInventoryCalculationMode calculationMode;
    public Integer stockProtectionQty;
    public BigDecimal calculationPercent;
}
