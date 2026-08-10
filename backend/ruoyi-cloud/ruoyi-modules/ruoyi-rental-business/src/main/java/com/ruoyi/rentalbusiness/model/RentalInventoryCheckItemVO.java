package com.ruoyi.rentalbusiness.model;

/** 单个资源池的库存校验结果。 */
public class RentalInventoryCheckItemVO
{
    public Long poolId;
    public String poolCode;
    public String poolName;
    public Integer requiredQty;
    public Integer availableQty;
    public Integer reservedQty;
    public Integer freeQty;
    public Boolean sufficient;
}
