package com.ruoyi.rentalbusiness.domain;

import java.time.LocalDateTime;

/**
 * 物流路由缓存的订单物流投影。
 *
 * <p>该对象只承载物流路由读取和刷新所需的字段，不作为浏览器端响应对象，避免把订单表
 * 和物流表的内部字段直接暴露给商户端。</p>
 */
public class LogisticsOrderTrackingRecord
{
    public Long logisticsId;
    public Long tenantId;
    public Long memberId;
    public Long orderId;
    public Integer direction;
    public String orderNo;
    public String carrier;
    public String trackingNo;
    public Integer routeStatus;
    public Integer abnormalFlag;
    public Integer routeAvailable;
    public String routeProvider;
    public String routeMessage;
    public LocalDateTime routeQueryTime;
    public String recipientPhone;
    /** 发货件使用收件地址，归还件使用归还仓详细地址。 */
    public String destinationAddress;
}
