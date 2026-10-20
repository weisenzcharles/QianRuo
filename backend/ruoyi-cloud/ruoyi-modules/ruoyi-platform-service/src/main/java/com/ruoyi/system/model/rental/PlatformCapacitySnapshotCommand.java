package com.ruoyi.system.model.rental;

/** 登记一条数据库、对象存储或文件存储容量快照。 */
public class PlatformCapacitySnapshotCommand
{
    private Integer scopeType;
    private String scopeKey;
    private String scopeName;
    private Long capacityBytes;
    private Long usedBytes;
    private Long availableBytes;
    private String snapshotSource;
    private String measuredTime;

    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
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
    public String getSnapshotSource() { return snapshotSource; }
    public void setSnapshotSource(String snapshotSource) { this.snapshotSource = snapshotSource; }
    public String getMeasuredTime() { return measuredTime; }
    public void setMeasuredTime(String measuredTime) { this.measuredTime = measuredTime; }
}
