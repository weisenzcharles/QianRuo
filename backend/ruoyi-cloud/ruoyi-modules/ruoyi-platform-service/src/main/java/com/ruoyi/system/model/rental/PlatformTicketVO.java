package com.ruoyi.system.model.rental;

/** 平台客服工单列表和详情视图。 */
public class PlatformTicketVO
{
    private Long ticketId;
    private String ticketNo;
    private Long memberId;
    private String memberName;
    private Integer ticketType;
    private String ticketTypeName;
    private String refType;
    private Long refId;
    private String title;
    private String content;
    private Integer ticketStatus;
    private String ticketStatusName;
    private Integer priority;
    private String priorityName;
    private Integer sourceType;
    private String sourceTypeName;
    private Long requesterUserId;
    private String requesterName;
    private Long assigneeUserId;
    private String assigneeName;
    private String createTime;
    private String updateTime;
    private String remark;

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Integer getTicketType() { return ticketType; }
    public void setTicketType(Integer ticketType) { this.ticketType = ticketType; }
    public String getTicketTypeName() { return ticketTypeName; }
    public void setTicketTypeName(String ticketTypeName) { this.ticketTypeName = ticketTypeName; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(Integer ticketStatus) { this.ticketStatus = ticketStatus; }
    public String getTicketStatusName() { return ticketStatusName; }
    public void setTicketStatusName(String ticketStatusName) { this.ticketStatusName = ticketStatusName; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getPriorityName() { return priorityName; }
    public void setPriorityName(String priorityName) { this.priorityName = priorityName; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public String getSourceTypeName() { return sourceTypeName; }
    public void setSourceTypeName(String sourceTypeName) { this.sourceTypeName = sourceTypeName; }
    public Long getRequesterUserId() { return requesterUserId; }
    public void setRequesterUserId(Long requesterUserId) { this.requesterUserId = requesterUserId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public Long getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
