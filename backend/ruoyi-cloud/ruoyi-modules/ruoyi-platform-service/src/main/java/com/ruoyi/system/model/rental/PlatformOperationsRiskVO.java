package com.ruoyi.system.model.rental;

import com.ruoyi.system.enums.PlatformOperationsRiskStatus;

/** 平台运营风险事项。 */
public class PlatformOperationsRiskVO
{
    private Long riskEventId;
    private Integer riskType;
    private String riskTypeName;
    private Integer riskLevel;
    private String riskLevelName;
    private String subject;
    private long count;
    private String detail;
    private Integer eventStatus = PlatformOperationsRiskStatus.PENDING.getCode();
    private String eventStatusName = PlatformOperationsRiskStatus.PENDING.getLabel();
    private String firstSeenTime;
    private String lastSeenTime;

    public Long getRiskEventId() { return riskEventId; }
    public void setRiskEventId(Long riskEventId) { this.riskEventId = riskEventId; }
    public Integer getRiskType() { return riskType; }
    public void setRiskType(Integer riskType) { this.riskType = riskType; }
    public String getRiskTypeName() { return riskTypeName; }
    public void setRiskTypeName(String riskTypeName) { this.riskTypeName = riskTypeName; }
    public Integer getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskLevelName() { return riskLevelName; }
    public void setRiskLevelName(String riskLevelName) { this.riskLevelName = riskLevelName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Integer getEventStatus() { return eventStatus; }
    public void setEventStatus(Integer eventStatus) { this.eventStatus = eventStatus; }
    public String getEventStatusName() { return eventStatusName; }
    public void setEventStatusName(String eventStatusName) { this.eventStatusName = eventStatusName; }
    public String getFirstSeenTime() { return firstSeenTime; }
    public void setFirstSeenTime(String firstSeenTime) { this.firstSeenTime = firstSeenTime; }
    public String getLastSeenTime() { return lastSeenTime; }
    public void setLastSeenTime(String lastSeenTime) { this.lastSeenTime = lastSeenTime; }
}
