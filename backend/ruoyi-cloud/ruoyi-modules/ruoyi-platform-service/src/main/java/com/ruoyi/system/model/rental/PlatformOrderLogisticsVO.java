package com.ruoyi.system.model.rental;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/** 平台订单详情中的物流单浏览对象。 */
public class PlatformOrderLogisticsVO
{
    private String directionName;
    private String carrier;
    private String trackingNo;
    private String sourceTypeName;
    private String routeStatusName;
    private String routeProvider;
    private String routeMessage;
    private String routeQueryTime;
    private String lastRouteTime;
    private BigDecimal feeAmount;
    private List<PlatformOrderRouteVO> routes = Collections.emptyList();

    public String getDirectionName() { return directionName; }
    public void setDirectionName(String value) { directionName = value; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String value) { carrier = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { trackingNo = value; }
    public String getSourceTypeName() { return sourceTypeName; }
    public void setSourceTypeName(String value) { sourceTypeName = value; }
    public String getRouteStatusName() { return routeStatusName; }
    public void setRouteStatusName(String value) { routeStatusName = value; }
    public String getRouteProvider() { return routeProvider; }
    public void setRouteProvider(String value) { routeProvider = value; }
    public String getRouteMessage() { return routeMessage; }
    public void setRouteMessage(String value) { routeMessage = value; }
    public String getRouteQueryTime() { return routeQueryTime; }
    public void setRouteQueryTime(String value) { routeQueryTime = value; }
    public String getLastRouteTime() { return lastRouteTime; }
    public void setLastRouteTime(String value) { lastRouteTime = value; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal value) { feeAmount = value; }
    public List<PlatformOrderRouteVO> getRoutes() { return routes; }
    public void setRoutes(List<PlatformOrderRouteVO> value) { routes = value == null ? Collections.emptyList() : value; }
}
