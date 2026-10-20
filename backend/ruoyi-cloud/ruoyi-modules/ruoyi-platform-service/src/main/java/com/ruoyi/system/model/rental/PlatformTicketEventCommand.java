package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 平台追加客服工单事件入参。 */
public class PlatformTicketEventCommand
{
    private Integer eventType;
    private Integer toStatus;
    private Long assigneeUserId;
    @NotBlank(message = "事件详情不能为空")
    @Size(max = 4000, message = "事件详情不能超过4000个字符")
    private String detail;

    public Integer getEventType() { return eventType; }
    public void setEventType(Integer eventType) { this.eventType = eventType; }
    public Integer getToStatus() { return toStatus; }
    public void setToStatus(Integer toStatus) { this.toStatus = toStatus; }
    public Long getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
