package com.ruoyi.rentalbusiness.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户物流治理列表的内部查询行。
 *
 * <p>该对象只在执行面服务内部使用，浏览器端通过专用 VO 获取展示字段。</p>
 */
public class MerchantLogisticsListRecord
{
    public Long logisticsId;
    public Long orderId;
    public Long warehouseId;
    public String orderNo;
    public Integer direction;
    public String carrier;
    public String trackingNo;
    public Integer sourceType;
    public LocalDateTime pickupTime;
    public LocalDateTime estimatedArriveTime;
    public Integer routeStatus;
    public Integer abnormalFlag;
    public Integer routeAvailable;
    public String routeProvider;
    public String routeMessage;
    public LocalDateTime routeQueryTime;
    public LocalDateTime lastRouteTime;
    public String warehouseName;
    public BigDecimal feeAmount;
    public LocalDateTime updateTime;
}
