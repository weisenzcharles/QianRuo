package com.ruoyi.system.model.rental;

/** 平台容量流水展示对象。 */
public class PlatformCapacityLedgerVO
{
    private Long storageLedgerId;
    private Integer scopeType;
    private String scopeTypeName;
    private String scopeKey;
    private String scopeName;
    private Long capacityBytes;
    private Long usedBytes;
    private Long availableBytes;
    private Long changedBytes;
    private Integer changeType;
    private String changeTypeName;
    private String snapshotSource;
    private String measuredTime;
    private String createTime;

    public Long getStorageLedgerId() { return storageLedgerId; }
    public void setStorageLedgerId(Long storageLedgerId) { this.storageLedgerId = storageLedgerId; }
    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
    public String getScopeTypeName() { return scopeTypeName; }
    public void setScopeTypeName(String scopeTypeName) { this.scopeTypeName = scopeTypeName; }
    public String getScopeKey() { return scopeKey; }
    public void setScopeKey(String scopeKey) { this.scopeKey = scopeKey; }
    public String getScopeName() { return scopeName; }
    public void setScopeName(String scopeName) { this.scopeName = scopeName; }
    public Long getCapacityBytes() { return capacityBytes; }
    public void setCapacityBytes(Long capacityBytes) { this.capacityBytes = capacityBytes; }
    public Long getUsedBytes() { return usedBytes; }
    public void setUsedBytes(Long usedBytes) { this.usedBytes = usedBytes; }
    public Long getAvailableBytes() { return availableBytes; }
    public void setAvailableBytes(Long availableBytes) { this.availableBytes = availableBytes; }
    public Long getChangedBytes() { return changedBytes; }
    public void setChangedBytes(Long changedBytes) { this.changedBytes = changedBytes; }
    public Integer getChangeType() { return changeType; }
    public void setChangeType(Integer changeType) { this.changeType = changeType; }
    public String getChangeTypeName() { return changeTypeName; }
    public void setChangeTypeName(String changeTypeName) { this.changeTypeName = changeTypeName; }
    public String getSnapshotSource() { return snapshotSource; }
    public void setSnapshotSource(String snapshotSource) { this.snapshotSource = snapshotSource; }
    public String getMeasuredTime() { return measuredTime; }
    public void setMeasuredTime(String measuredTime) { this.measuredTime = measuredTime; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
