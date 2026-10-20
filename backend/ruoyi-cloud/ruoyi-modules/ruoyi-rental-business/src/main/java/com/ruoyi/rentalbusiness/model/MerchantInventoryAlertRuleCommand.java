package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryAlertRuleStatus;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryAlertThresholdType;

/** 新增或修改商户库存告警规则命令。 */
public class MerchantInventoryAlertRuleCommand
{
    public Long ruleId;
    public Long warehouseId;
    public Long productId;
    public MerchantInventoryAlertThresholdType thresholdType;
    public BigDecimal thresholdValue;
    public Boolean notifyEnabled;
    public MerchantInventoryAlertRuleStatus ruleStatus;
}
