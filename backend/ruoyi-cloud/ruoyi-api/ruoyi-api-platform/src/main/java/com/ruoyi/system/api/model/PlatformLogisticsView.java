package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台物流治理列表视图。编号字段仅供平台服务内部详情和刷新使用。 */
public class PlatformLogisticsView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long logisticsId;
    private Long tenantId;
    private Long memberId;
    private Long orderId;
    private String orderNo;
    private String merchantName;
    private String merchantCode;
    private String warehouseName;
    private Integer direction;
    private String directionName;
    private String carrier;
    private String trackingNo;
    private Integer sourceType;
    private String sourceTypeName;
    private Integer routeStatus;
    private String routeStatusName;
    private Integer abnormalFlag;
    private Integer routeAvailable;
    private String routeProvider;
    private String routeMessage;
    private String pickupTime;
    private String estimatedArriveTime;
    private String routeQueryTime;
    private String lastRouteTime;
    private BigDecimal feeAmount;
    private String updateTime;

    public Long getLogisticsId() { return logisticsId; }
    public void setLogisticsId(Long value) { logisticsId = value; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long value) { tenantId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String value) { merchantName = value; }
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String value) { merchantCode = value; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String value) { warehouseName = value; }
    public Integer getDirection() { return direction; }
    public void setDirection(Integer value) { direction = value; }
    public String getDirectionName() { return directionName; }
    public void setDirectionName(String value) { directionName = value; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String value) { carrier = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { trackingNo = value; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer value) { sourceType = value; }
    public String getSourceTypeName() { return sourceTypeName; }
    public void setSourceTypeName(String value) { sourceTypeName = value; }
    public Integer getRouteStatus() { return routeStatus; }
    public void setRouteStatus(Integer value) { routeStatus = value; }
    public String getRouteStatusName() { return routeStatusName; }
    public void setRouteStatusName(String value) { routeStatusName = value; }
    public Integer getAbnormalFlag() { return abnormalFlag; }
    public void setAbnormalFlag(Integer value) { abnormalFlag = value; }
    public Integer getRouteAvailable() { return routeAvailable; }
    public void setRouteAvailable(Integer value) { routeAvailable = value; }
    public String getRouteProvider() { return routeProvider; }
    public void setRouteProvider(String value) { routeProvider = value; }
    public String getRouteMessage() { return routeMessage; }
    public void setRouteMessage(String value) { routeMessage = value; }
    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String value) { pickupTime = value; }
    public String getEstimatedArriveTime() { return estimatedArriveTime; }
    public void setEstimatedArriveTime(String value) { estimatedArriveTime = value; }
    public String getRouteQueryTime() { return routeQueryTime; }
    public void setRouteQueryTime(String value) { routeQueryTime = value; }
    public String getLastRouteTime() { return lastRouteTime; }
    public void setLastRouteTime(String value) { lastRouteTime = value; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal value) { feeAmount = value; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String value) { updateTime = value; }
}
