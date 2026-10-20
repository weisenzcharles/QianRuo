package com.ruoyi.system.model.rental;

/** 平台订单详情中的售后处理事件。 */
public class PlatformOrderAfterSalesEventVO
{
    private String eventTypeName;
    private String fromStatusName;
    private String toStatusName;
    private String content;
    private String operatorName;
    private String createTime;

    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String value) { eventTypeName = value; }
    public String getFromStatusName() { return fromStatusName; }
    public void setFromStatusName(String value) { fromStatusName = value; }
    public String getToStatusName() { return toStatusName; }
    public void setToStatusName(String value) { toStatusName = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String value) { operatorName = value; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
}
