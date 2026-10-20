package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台查看的联盟订单事件流水。 */
public class PlatformAllianceOrderEventView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long eventId;
    private Integer eventType;
    private Integer fromStatus;
    private Integer toStatus;
    private Long operatorMemberId;
    private Long operatorUserId;
    private String operatorMerchantName;
    private String eventDataJson;
    private String createTime;

    public Long getEventId() { return eventId; }
    public void setEventId(Long value) { eventId = value; }
    public Integer getEventType() { return eventType; }
    public void setEventType(Integer value) { eventType = value; }
    public Integer getFromStatus() { return fromStatus; }
    public void setFromStatus(Integer value) { fromStatus = value; }
    public Integer getToStatus() { return toStatus; }
    public void setToStatus(Integer value) { toStatus = value; }
    public Long getOperatorMemberId() { return operatorMemberId; }
    public void setOperatorMemberId(Long value) { operatorMemberId = value; }
    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long value) { operatorUserId = value; }
    public String getOperatorMerchantName() { return operatorMerchantName; }
    public void setOperatorMerchantName(String value) { operatorMerchantName = value; }
    public String getEventDataJson() { return eventDataJson; }
    public void setEventDataJson(String value) { eventDataJson = value; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
}
