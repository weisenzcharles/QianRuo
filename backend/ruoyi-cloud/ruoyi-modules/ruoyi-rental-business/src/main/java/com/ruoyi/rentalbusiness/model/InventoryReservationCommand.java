package com.ruoyi.rentalbusiness.model;

/** 库存预占、释放和消耗命令。 */
public class InventoryReservationCommand
{
    public Long reservationId;
    public Long orderId;
    public Long packageId;
    /** 商品级预占时使用；套餐级预占仍只使用 packageId。 */
    public Long productId;
    public Long warehouseId;
    public Integer quantity;
    public String idempotencyKey;
    public String reason;
}
