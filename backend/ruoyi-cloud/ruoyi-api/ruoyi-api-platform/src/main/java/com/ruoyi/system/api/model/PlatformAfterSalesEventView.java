package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Date;

/** 平台售后处理时间线视图。 */
public class PlatformAfterSalesEventView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer eventType;
    private String eventTypeName;
    private Integer fromStatus;
    private String fromStatusName;
    private Integer toStatus;
    private String toStatusName;
    private String content;
    private String operatorName;
    private Date createTime;

    public Integer getEventType() { return eventType; }
    public void setEventType(Integer value) { eventType = value; }
    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String value) { eventTypeName = value; }
    public Integer getFromStatus() { return fromStatus; }
    public void setFromStatus(Integer value) { fromStatus = value; }
    public String getFromStatusName() { return fromStatusName; }
    public void setFromStatusName(String value) { fromStatusName = value; }
    public Integer getToStatus() { return toStatus; }
    public void setToStatus(Integer value) { toStatus = value; }
    public String getToStatusName() { return toStatusName; }
    public void setToStatusName(String value) { toStatusName = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String value) { operatorName = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
}
