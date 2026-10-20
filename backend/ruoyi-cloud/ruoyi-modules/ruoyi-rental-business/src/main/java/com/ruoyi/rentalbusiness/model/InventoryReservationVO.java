package com.ruoyi.rentalbusiness.model;

import java.util.ArrayList;
import java.util.List;

/** 商户库存预占及序列号明细展示对象。 */
public class InventoryReservationVO
{
    public Long reservationId;
    public Long orderId;
    public Long packageId;
    public Long warehouseId;
    public Integer quantity;
    public Integer reservationStatus;
    public Long packageVersionId;
    public String reservationNo;
    public String idempotencyKey;
    public List<InventoryReservationItemVO> items = new ArrayList<InventoryReservationItemVO>();

    /** 预占库存单元明细。 */
    public static class InventoryReservationItemVO
    {
        public Long inventoryId;
        public Long unitId;
        public Integer quantity;
        public Integer unitStatus;
    }
}
