package com.ruoyi.system.model.rental;

/** 平台回收站查询条件。 */
public class PlatformRecycleQuery
{
    private String objectType;
    private Integer restoreStatus;
    private String keyword;

    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public Integer getRestoreStatus() { return restoreStatus; }
    public void setRestoreStatus(Integer restoreStatus) { this.restoreStatus = restoreStatus; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
