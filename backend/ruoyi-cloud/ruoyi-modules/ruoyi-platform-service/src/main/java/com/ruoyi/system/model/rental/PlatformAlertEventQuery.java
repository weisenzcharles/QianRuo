package com.ruoyi.system.model.rental;

/** 平台运维告警查询条件。 */
public class PlatformAlertEventQuery
{
    private Integer alertType;
    private Integer alertLevel;
    private Integer eventStatus;
    private String keyword;

    public Integer getAlertType() { return alertType; }
    public void setAlertType(Integer alertType) { this.alertType = alertType; }
    public Integer getAlertLevel() { return alertLevel; }
    public void setAlertLevel(Integer alertLevel) { this.alertLevel = alertLevel; }
    public Integer getEventStatus() { return eventStatus; }
    public void setEventStatus(Integer eventStatus) { this.eventStatus = eventStatus; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
