package com.ruoyi.rentalbusiness.model;

/** 库存预占、释放和消耗命令。 */
public class InventoryReservationCommand
{
    public Long reservationId;
    public Long orderId;
    public Long packageId;
    public Long warehouseId;
    public Integer quantity;
    public String idempotencyKey;
    public String reason;
}
