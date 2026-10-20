package com.ruoyi.system.model.rental;

/** 平台回收站记录视图。快照只按权限返回，不直接执行任意 SQL 恢复。 */
public class PlatformRecycleRecordVO
{
    private Long recycleId;
    private String objectType;
    private Long objectId;
    private String objectCode;
    private String objectName;
    private Long memberId;
    private String memberName;
    private String snapshotJson;
    private Long deletedBy;
    private String deletedTime;
    private String expiresTime;
    private Integer restoreStatus;
    private String restoreStatusName;
    private String restoredTime;
    private String restoreRemark;

    public Long getRecycleId() { return recycleId; }
    public void setRecycleId(Long recycleId) { this.recycleId = recycleId; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public Long getObjectId() { return objectId; }
    public void setObjectId(Long objectId) { this.objectId = objectId; }
    public String getObjectCode() { return objectCode; }
    public void setObjectCode(String objectCode) { this.objectCode = objectCode; }
    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
    public String getDeletedTime() { return deletedTime; }
    public void setDeletedTime(String deletedTime) { this.deletedTime = deletedTime; }
    public String getExpiresTime() { return expiresTime; }
    public void setExpiresTime(String expiresTime) { this.expiresTime = expiresTime; }
    public Integer getRestoreStatus() { return restoreStatus; }
    public void setRestoreStatus(Integer restoreStatus) { this.restoreStatus = restoreStatus; }
    public String getRestoreStatusName() { return restoreStatusName; }
    public void setRestoreStatusName(String restoreStatusName) { this.restoreStatusName = restoreStatusName; }
    public String getRestoredTime() { return restoredTime; }
    public void setRestoredTime(String restoredTime) { this.restoredTime = restoredTime; }
    public String getRestoreRemark() { return restoreRemark; }
    public void setRestoreRemark(String restoreRemark) { this.restoreRemark = restoreRemark; }
}
