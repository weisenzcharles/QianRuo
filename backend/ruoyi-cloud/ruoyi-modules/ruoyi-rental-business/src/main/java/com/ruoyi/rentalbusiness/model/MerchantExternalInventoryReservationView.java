package com.ruoyi.rentalbusiness.model;

import java.time.LocalDateTime;

/** 外部平台库存预占响应。只返回外部系统继续操作所需的业务字段。 */
public class MerchantExternalInventoryReservationView
{
    public String externalOrderNo;
    public String reservationNo;
    public Long packageId;
    public Long productId;
    public Long warehouseId;
    public Integer quantity;
    public Integer reservationStatus;
    public String reservationStatusLabel;
    public LocalDateTime expireTime;
    public boolean replayed;
}
