package com.ruoyi.system.model.rental;

/** 平台备份记录展示对象。 */
public class PlatformBackupRecordVO
{
    private Long backupRecordId;
    private String backupNo;
    private Integer backupType;
    private String backupTypeName;
    private Integer backupStatus;
    private String backupStatusName;
    private String backupKey;
    private String fileName;
    private Long fileSizeBytes;
    private String startedTime;
    private String completedTime;
    private String retentionUntil;
    private String failureReason;
    private String createTime;

    public Long getBackupRecordId() { return backupRecordId; }
    public void setBackupRecordId(Long backupRecordId) { this.backupRecordId = backupRecordId; }
    public String getBackupNo() { return backupNo; }
    public void setBackupNo(String backupNo) { this.backupNo = backupNo; }
    public Integer getBackupType() { return backupType; }
    public void setBackupType(Integer backupType) { this.backupType = backupType; }
    public String getBackupTypeName() { return backupTypeName; }
    public void setBackupTypeName(String backupTypeName) { this.backupTypeName = backupTypeName; }
    public Integer getBackupStatus() { return backupStatus; }
    public void setBackupStatus(Integer backupStatus) { this.backupStatus = backupStatus; }
    public String getBackupStatusName() { return backupStatusName; }
    public void setBackupStatusName(String backupStatusName) { this.backupStatusName = backupStatusName; }
    public String getBackupKey() { return backupKey; }
    public void setBackupKey(String backupKey) { this.backupKey = backupKey; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getStartedTime() { return startedTime; }
    public void setStartedTime(String startedTime) { this.startedTime = startedTime; }
    public String getCompletedTime() { return completedTime; }
    public void setCompletedTime(String completedTime) { this.completedTime = completedTime; }
    public String getRetentionUntil() { return retentionUntil; }
    public void setRetentionUntil(String retentionUntil) { this.retentionUntil = retentionUntil; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
