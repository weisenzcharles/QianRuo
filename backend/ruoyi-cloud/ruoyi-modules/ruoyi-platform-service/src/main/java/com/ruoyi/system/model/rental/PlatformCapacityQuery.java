package com.ruoyi.system.model.rental;

/** 平台容量流水查询条件。 */
public class PlatformCapacityQuery
{
    private Integer scopeType;
    private String keyword;

    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
