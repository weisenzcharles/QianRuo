package com.ruoyi.merchant.model;

import java.util.Date;

/** 商户内部通知列表和详情视图。 */
public class MerchantInternalNoticeVO
{
    private Long internalNoticeId;
    private String noticeTitle;
    private Integer noticeType;
    private String noticeTypeName;
    private String noticeContent;
    private Integer targetScope;
    private String targetScopeName;
    private Integer status;
    private String statusName;
    private Date effectiveTime;
    private Date offlineTime;
    private String roleNames;
    private Long[] roleIds;
    private Integer isRead;
    private Date createTime;
    private Date updateTime;
    private String remark;

    public Long getInternalNoticeId() { return internalNoticeId; }
    public void setInternalNoticeId(Long internalNoticeId) { this.internalNoticeId = internalNoticeId; }
    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }
    public Integer getNoticeType() { return noticeType; }
    public void setNoticeType(Integer noticeType) { this.noticeType = noticeType; }
    public String getNoticeTypeName() { return noticeTypeName; }
    public void setNoticeTypeName(String noticeTypeName) { this.noticeTypeName = noticeTypeName; }
    public String getNoticeContent() { return noticeContent; }
    public void setNoticeContent(String noticeContent) { this.noticeContent = noticeContent; }
    public Integer getTargetScope() { return targetScope; }
    public void setTargetScope(Integer targetScope) { this.targetScope = targetScope; }
    public String getTargetScopeName() { return targetScopeName; }
    public void setTargetScopeName(String targetScopeName) { this.targetScopeName = targetScopeName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public Date getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(Date effectiveTime) { this.effectiveTime = effectiveTime; }
    public Date getOfflineTime() { return offlineTime; }
    public void setOfflineTime(Date offlineTime) { this.offlineTime = offlineTime; }
    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }
    public Long[] getRoleIds() { return roleIds; }
    public void setRoleIds(Long[] roleIds) { this.roleIds = roleIds; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
