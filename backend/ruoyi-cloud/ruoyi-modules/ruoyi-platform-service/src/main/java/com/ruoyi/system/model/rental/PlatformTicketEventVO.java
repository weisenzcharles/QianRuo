package com.ruoyi.system.model.rental;

/** 平台客服工单时间线事件视图。 */
public class PlatformTicketEventVO
{
    private Long eventId;
    private Integer eventType;
    private String eventTypeName;
    private Integer fromStatus;
    private String fromStatusName;
    private Integer toStatus;
    private String toStatusName;
    private String detail;
    private Long actorUserId;
    private String actorName;
    private String createTime;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Integer getEventType() { return eventType; }
    public void setEventType(Integer eventType) { this.eventType = eventType; }
    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String eventTypeName) { this.eventTypeName = eventTypeName; }
    public Integer getFromStatus() { return fromStatus; }
    public void setFromStatus(Integer fromStatus) { this.fromStatus = fromStatus; }
    public String getFromStatusName() { return fromStatusName; }
    public void setFromStatusName(String fromStatusName) { this.fromStatusName = fromStatusName; }
    public Integer getToStatus() { return toStatus; }
    public void setToStatus(Integer toStatus) { this.toStatus = toStatus; }
    public String getToStatusName() { return toStatusName; }
    public void setToStatusName(String toStatusName) { this.toStatusName = toStatusName; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
