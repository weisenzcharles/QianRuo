package com.ruoyi.merchant.model;

import java.util.Date;

/** 商户内部通知写入命令。 */
public class MerchantInternalNoticeCommand
{
    private Long internalNoticeId;
    private String noticeTitle;
    private Integer noticeType;
    private String noticeContent;
    private Integer targetScope;
    private Integer status;
    private Date effectiveTime;
    private Date offlineTime;
    private Long[] roleIds;
    private String remark;

    public Long getInternalNoticeId() { return internalNoticeId; }
    public void setInternalNoticeId(Long internalNoticeId) { this.internalNoticeId = internalNoticeId; }
    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }
    public Integer getNoticeType() { return noticeType; }
    public void setNoticeType(Integer noticeType) { this.noticeType = noticeType; }
    public String getNoticeContent() { return noticeContent; }
    public void setNoticeContent(String noticeContent) { this.noticeContent = noticeContent; }
    public Integer getTargetScope() { return targetScope; }
    public void setTargetScope(Integer targetScope) { this.targetScope = targetScope; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(Date effectiveTime) { this.effectiveTime = effectiveTime; }
    public Date getOfflineTime() { return offlineTime; }
    public void setOfflineTime(Date offlineTime) { this.offlineTime = offlineTime; }
    public Long[] getRoleIds() { return roleIds; }
    public void setRoleIds(Long[] roleIds) { this.roleIds = roleIds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
