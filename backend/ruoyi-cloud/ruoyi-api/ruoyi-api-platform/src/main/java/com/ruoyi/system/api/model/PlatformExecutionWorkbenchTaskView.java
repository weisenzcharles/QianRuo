package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 租赁业务服务提供的平台执行面待办视图。 */
public class PlatformExecutionWorkbenchTaskView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Integer taskType;
    private Integer priority;
    private Integer taskStatus;
    private String taskStatusName;
    private String subject;
    private String detail;
    private String businessNo;
    private String merchantCode;
    private String merchantName;
    private String createTime;
    private String updateTime;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Integer getTaskType() { return taskType; }
    public void setTaskType(Integer taskType) { this.taskType = taskType; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Integer getTaskStatus() { return taskStatus; }
    public void setTaskStatus(Integer taskStatus) { this.taskStatus = taskStatus; }
    public String getTaskStatusName() { return taskStatusName; }
    public void setTaskStatusName(String taskStatusName) { this.taskStatusName = taskStatusName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String businessNo) { this.businessNo = businessNo; }
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { this.merchantCode = merchantCode; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
