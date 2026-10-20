package com.ruoyi.system.model.rental;

/** 平台客服工单指派记录视图。 */
public class PlatformTicketAssignmentVO
{
    private Long assignmentId;
    private Long assigneeUserId;
    private String assigneeName;
    private Integer assignmentStatus;
    private String assignmentStatusName;
    private String assignedTime;
    private String completedTime;

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public Long getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public Integer getAssignmentStatus() { return assignmentStatus; }
    public void setAssignmentStatus(Integer assignmentStatus) { this.assignmentStatus = assignmentStatus; }
    public String getAssignmentStatusName() { return assignmentStatusName; }
    public void setAssignmentStatusName(String assignmentStatusName) { this.assignmentStatusName = assignmentStatusName; }
    public String getAssignedTime() { return assignedTime; }
    public void setAssignedTime(String assignedTime) { this.assignedTime = assignedTime; }
    public String getCompletedTime() { return completedTime; }
    public void setCompletedTime(String completedTime) { this.completedTime = completedTime; }
}
