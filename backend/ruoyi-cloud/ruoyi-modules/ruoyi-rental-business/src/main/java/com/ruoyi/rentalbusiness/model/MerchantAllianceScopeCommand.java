package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户联盟共享范围维护命令。归属字段由登录上下文确定，不接受客户端传入。 */
public class MerchantAllianceScopeCommand
{
    public Long warehouseId;
    public Long productId;
    public BigDecimal shareQuantityRatio;
    public Integer shareQuantityLimit;
    public Integer acceptOrderEnabled;
    public Integer shareStatus;
    public String remark;
}
