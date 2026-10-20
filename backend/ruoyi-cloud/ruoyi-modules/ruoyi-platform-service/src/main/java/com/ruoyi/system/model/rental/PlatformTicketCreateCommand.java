package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 平台创建客服工单入参。 */
public class PlatformTicketCreateCommand
{
    private Long memberId;
    private Integer ticketType;
    private String refType;
    private Long refId;
    @NotBlank(message = "工单标题不能为空")
    @Size(max = 160, message = "工单标题不能超过160个字符")
    private String title;
    @NotBlank(message = "工单内容不能为空")
    @Size(max = 4000, message = "工单内容不能超过4000个字符")
    private String content;
    private Integer priority;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Integer getTicketType() { return ticketType; }
    public void setTicketType(Integer ticketType) { this.ticketType = ticketType; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
