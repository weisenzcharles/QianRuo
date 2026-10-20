package com.ruoyi.system.model.rental;

/** 平台运维台账摘要。 */
public class PlatformOperationsLedgerSummaryVO
{
    private PlatformBackupRecordVO latestSuccessfulBackup;
    private Long backupSuccessCount;
    private Long backupFailureCount;
    private Long latestUsedBytes;
    private Long latestAvailableBytes;
    private String latestMeasuredTime;

    public PlatformBackupRecordVO getLatestSuccessfulBackup() { return latestSuccessfulBackup; }
    public void setLatestSuccessfulBackup(PlatformBackupRecordVO latestSuccessfulBackup) { this.latestSuccessfulBackup = latestSuccessfulBackup; }
    public Long getBackupSuccessCount() { return backupSuccessCount; }
    public void setBackupSuccessCount(Long backupSuccessCount) { this.backupSuccessCount = backupSuccessCount; }
    public Long getBackupFailureCount() { return backupFailureCount; }
    public void setBackupFailureCount(Long backupFailureCount) { this.backupFailureCount = backupFailureCount; }
    public Long getLatestUsedBytes() { return latestUsedBytes; }
    public void setLatestUsedBytes(Long latestUsedBytes) { this.latestUsedBytes = latestUsedBytes; }
    public Long getLatestAvailableBytes() { return latestAvailableBytes; }
    public void setLatestAvailableBytes(Long latestAvailableBytes) { this.latestAvailableBytes = latestAvailableBytes; }
    public String getLatestMeasuredTime() { return latestMeasuredTime; }
    public void setLatestMeasuredTime(String latestMeasuredTime) { this.latestMeasuredTime = latestMeasuredTime; }
}
