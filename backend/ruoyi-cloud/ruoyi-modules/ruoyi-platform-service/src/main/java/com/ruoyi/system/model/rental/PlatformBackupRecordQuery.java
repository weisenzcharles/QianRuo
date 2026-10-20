package com.ruoyi.system.model.rental;

/** 平台备份记录查询条件。 */
public class PlatformBackupRecordQuery
{
    private Integer backupType;
    private Integer backupStatus;
    private String keyword;

    public Integer getBackupType() { return backupType; }
    public void setBackupType(Integer backupType) { this.backupType = backupType; }
    public Integer getBackupStatus() { return backupStatus; }
    public void setBackupStatus(Integer backupStatus) { this.backupStatus = backupStatus; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
