package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台物流详情中的供应商调用审计视图，快递单号只返回脱敏值。 */
public class PlatformLogisticsCallView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String provider;
    private String providerName;
    private String carrier;
    private String trackingNoMasked;
    private Integer available;
    private String availableName;
    private String providerStatus;
    private Integer routeStatus;
    private String routeStatusName;
    private String responseMessage;
    private Long durationMs;
    private String errorType;
    private String errorTypeName;
    private String createTime;

    public String getProvider() { return provider; }
    public void setProvider(String value) { provider = value; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String value) { providerName = value; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String value) { carrier = value; }
    public String getTrackingNoMasked() { return trackingNoMasked; }
    public void setTrackingNoMasked(String value) { trackingNoMasked = value; }
    public Integer getAvailable() { return available; }
    public void setAvailable(Integer value) { available = value; }
    public String getAvailableName() { return availableName; }
    public void setAvailableName(String value) { availableName = value; }
    public String getProviderStatus() { return providerStatus; }
    public void setProviderStatus(String value) { providerStatus = value; }
    public Integer getRouteStatus() { return routeStatus; }
    public void setRouteStatus(Integer value) { routeStatus = value; }
    public String getRouteStatusName() { return routeStatusName; }
    public void setRouteStatusName(String value) { routeStatusName = value; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String value) { responseMessage = value; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long value) { durationMs = value; }
    public String getErrorType() { return errorType; }
    public void setErrorType(String value) { errorType = value; }
    public String getErrorTypeName() { return errorTypeName; }
    public void setErrorTypeName(String value) { errorTypeName = value; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
}
