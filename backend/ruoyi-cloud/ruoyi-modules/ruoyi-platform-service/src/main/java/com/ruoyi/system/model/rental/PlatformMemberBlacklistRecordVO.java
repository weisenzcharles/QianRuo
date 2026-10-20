package com.ruoyi.system.model.rental;

/** 平台展示的商户黑名单记录。 */
public class PlatformMemberBlacklistRecordVO
{
    private String recordNo;
    private Integer reasonType;
    private String reasonTypeName;
    private Integer blacklistStatus;
    private String blacklistStatusName;
    private String reason;
    private String effectiveTime;
    private String expiresTime;
    private String releasedTime;
    private String releaseRemark;
    private String createTime;

    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public Integer getReasonType() { return reasonType; }
    public void setReasonType(Integer reasonType) { this.reasonType = reasonType; }
    public String getReasonTypeName() { return reasonTypeName; }
    public void setReasonTypeName(String reasonTypeName) { this.reasonTypeName = reasonTypeName; }
    public Integer getBlacklistStatus() { return blacklistStatus; }
    public void setBlacklistStatus(Integer blacklistStatus) { this.blacklistStatus = blacklistStatus; }
    public String getBlacklistStatusName() { return blacklistStatusName; }
    public void setBlacklistStatusName(String blacklistStatusName) { this.blacklistStatusName = blacklistStatusName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(String effectiveTime) { this.effectiveTime = effectiveTime; }
    public String getExpiresTime() { return expiresTime; }
    public void setExpiresTime(String expiresTime) { this.expiresTime = expiresTime; }
    public String getReleasedTime() { return releasedTime; }
    public void setReleasedTime(String releasedTime) { this.releasedTime = releasedTime; }
    public String getReleaseRemark() { return releaseRemark; }
    public void setReleaseRemark(String releaseRemark) { this.releaseRemark = releaseRemark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
