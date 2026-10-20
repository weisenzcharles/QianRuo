package com.ruoyi.rentalbusiness.enums;

/** 联盟协助订单事件类型，与 ry_rental_order_alliance_event.event_type 保持映射。 */
public enum MerchantAllianceOrderEventType
{
    CREATED(1), ACCEPTED(2), REJECTED(3), TRANSFERRED(4), PUBLIC_POOL(5), CHILD_ORDER_CREATED(6),
    SHIPPED(7), COMPLETED(8), CANCELLED(9), EXCEPTION(10), WAREHOUSE_CHANGED(11);

    private final int code;

    MerchantAllianceOrderEventType(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
