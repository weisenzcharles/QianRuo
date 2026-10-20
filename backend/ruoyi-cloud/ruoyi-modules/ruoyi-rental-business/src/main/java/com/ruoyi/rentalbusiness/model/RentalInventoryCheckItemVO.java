package com.ruoyi.rentalbusiness.model;

/** 单个商品组件的库存校验结果。 */
public class RentalInventoryCheckItemVO
{
    public Long productId;
    public Integer requiredQty;
    public Integer availableQty;
    public Integer reservedQty;
    public Integer freeQty;
    public Boolean sufficient;
}
