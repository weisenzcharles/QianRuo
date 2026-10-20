package com.ruoyi.system.model.rental;

/** 登记一次数据库、日志或对象存储备份结果。 */
public class PlatformBackupRecordCommand
{
    private Integer backupType;
    private Integer backupStatus;
    private String backupKey;
    private String fileName;
    private Long fileSizeBytes;
    private String startedTime;
    private String completedTime;
    private String retentionUntil;
    private String failureReason;

    public Integer getBackupType() { return backupType; }
    public void setBackupType(Integer backupType) { this.backupType = backupType; }
    public Integer getBackupStatus() { return backupStatus; }
    public void setBackupStatus(Integer backupStatus) { this.backupStatus = backupStatus; }
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
}
