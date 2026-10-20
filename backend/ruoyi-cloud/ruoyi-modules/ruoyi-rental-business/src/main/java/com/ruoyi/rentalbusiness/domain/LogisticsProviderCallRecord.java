package com.ruoyi.rentalbusiness.domain;

/** 物流供应商调用审计记录，仅供持久层使用，不直接作为浏览器端响应。 */
public class LogisticsProviderCallRecord
{
    public Long tenantId;
    public Long memberId;
    public Long orderId;
    public Long logisticsId;
    public Integer direction;
    public String provider;
    public String carrier;
    public String trackingNoMasked;
    public String trackingNoHash;
    public Integer available;
    public String providerStatus;
    public Integer routeStatus;
    public String responseMessage;
    public Long durationMs;
    public String errorType;
    public Long operatorId;
}
