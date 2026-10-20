package com.ruoyi.rentalbusiness.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 收发趋势数字对应的订单明细响应。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantInboundOutboundOrderVO
{
    /** 仅供订单编码跳转订单详情使用，不直接作为用户可读字段展示。 */
    public Long orderId;
    public String orderNo;
    public String customerName;
    public String recipientName;
    public String recipientPhone;
    public String itemName;
    public String packageName;
    public Integer quantity;
    public String warehouseName;
    public String trendTime;
    public Integer orderStatus;
}
