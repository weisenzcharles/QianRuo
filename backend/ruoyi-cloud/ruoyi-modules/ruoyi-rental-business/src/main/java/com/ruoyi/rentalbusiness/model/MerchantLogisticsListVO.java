package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 商户物流治理列表展示对象。
 *
 * <p>订单唯一编号和物流唯一编号仅用于服务端内部关联，不返回给页面；页面操作使用订单编码和物流方向。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantLogisticsListVO
{
    @JsonIgnore
    public Long logisticsId;
    @JsonIgnore
    public Long orderId;
    public String orderNo;
    public Integer direction;
    public String directionName;
    public String carrier;
    public String trackingNo;
    public Integer sourceType;
    public String sourceTypeName;
    public LocalDateTime pickupTime;
    public LocalDateTime estimatedArriveTime;
    public Integer routeStatus;
    public String routeStatusName;
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
