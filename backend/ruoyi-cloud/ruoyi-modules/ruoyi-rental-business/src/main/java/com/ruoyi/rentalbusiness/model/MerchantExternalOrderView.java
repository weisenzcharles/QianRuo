package com.ruoyi.rentalbusiness.model;

import java.time.LocalDateTime;

/** 外部正式订单创建响应，只返回外部系统继续处理所需的业务标识。 */
public class MerchantExternalOrderView
{
    public String requestId;
    public boolean replayed;
    public String orderNo;
    public String externalOrderNo;
    public String reservationNo;
    public Integer orderStatus;
    public String orderStatusLabel;
    public LocalDateTime createTime;
    public boolean callbackQueued;
}
