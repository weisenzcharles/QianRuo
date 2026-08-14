package com.ruoyi.rentalbusiness.model;

import java.util.ArrayList;
import java.util.List;

/** 库存预占及资源池明细展示对象。 */
public class InventoryReservationVO
{
    public Long reservationId;
    public Long orderId;
    public Long packageId;
    public Long warehouseId;
    public Integer quantity;
    public String reservationStatus;
    public String idempotencyKey;
    public List<InventoryReservationItemVO> items = new ArrayList<InventoryReservationItemVO>();

    /** 预占资源池明细。 */
    public static class InventoryReservationItemVO
    {
        public Long poolId;
        public Integer requiredQty;
        public String reservationStatus;
    }
}
