package com.ruoyi.rentalbusiness.model;

/** 数量库存和逐件库存状态机变更命令。 */
public class InventoryTransitionCommand
{
    public Long productId;
    public Long unitId;
    public Long snId;
    public Long warehouseId;
    public String unitCode;
    public String snCode;
    public Integer fromStatus;
    public Integer toStatus;
    public Integer changeQty;
    public String idempotencyKey;
    public String reason;
}
