package com.ruoyi.rentalbusiness.model;

/** 收发趋势中单个商品或套餐在某日的订单计划和物流预计数量。 */
public class MerchantInboundOutboundTrendRowVO
{
    public Long itemId;
    public String itemName;
    public String itemBrand;
    public String itemCode;
    public String trendDate;
    public Long shipQuantity;
    public Long returnQuantity;
    /** 物流快照中的预计发货数量，日期取预约取件时间。 */
    public Long estimatedShipQuantity;
    /** 物流快照中的预计归还数量，日期取预计到达时间。 */
    public Long estimatedReturnQuantity;
}
