package com.ruoyi.system.model.rental;

/** 平台运维告警事件展示对象。 */
public class PlatformAlertEventVO
{
    private Long alertEventId;
    private Integer alertType;
    private String alertTypeName;
    private Integer alertLevel;
    private String alertLevelName;
    private String title;
    private String detail;
    private String sourceType;
    private String sourceKey;
    private Long occurrenceCount;
    private Integer eventStatus;
    private String eventStatusName;
    private String firstSeenTime;
    private String lastSeenTime;
    private String ackTime;
    private String resolvedTime;
    private String resolvedRemark;

    public Long getAlertEventId() { return alertEventId; }
    public void setAlertEventId(Long alertEventId) { this.alertEventId = alertEventId; }
    public Integer getAlertType() { return alertType; }
    public void setAlertType(Integer alertType) { this.alertType = alertType; }
    public String getAlertTypeName() { return alertTypeName; }
    public void setAlertTypeName(String alertTypeName) { this.alertTypeName = alertTypeName; }
    public Integer getAlertLevel() { return alertLevel; }
    public void setAlertLevel(Integer alertLevel) { this.alertLevel = alertLevel; }
    public String getAlertLevelName() { return alertLevelName; }
    public void setAlertLevelName(String alertLevelName) { this.alertLevelName = alertLevelName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public Long getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(Long occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public Integer getEventStatus() { return eventStatus; }
    public void setEventStatus(Integer eventStatus) { this.eventStatus = eventStatus; }
    public String getEventStatusName() { return eventStatusName; }
    public void setEventStatusName(String eventStatusName) { this.eventStatusName = eventStatusName; }
    public String getFirstSeenTime() { return firstSeenTime; }
    public void setFirstSeenTime(String firstSeenTime) { this.firstSeenTime = firstSeenTime; }
    public String getLastSeenTime() { return lastSeenTime; }
    public void setLastSeenTime(String lastSeenTime) { this.lastSeenTime = lastSeenTime; }
    public String getAckTime() { return ackTime; }
    public void setAckTime(String ackTime) { this.ackTime = ackTime; }
    public String getResolvedTime() { return resolvedTime; }
    public void setResolvedTime(String resolvedTime) { this.resolvedTime = resolvedTime; }
    public String getResolvedRemark() { return resolvedRemark; }
    public void setResolvedRemark(String resolvedRemark) { this.resolvedRemark = resolvedRemark; }
}
