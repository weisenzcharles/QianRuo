package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台物流赔付单查询条件。 */
public class PlatformLogisticsClaimQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer claimType;
    private Integer claimStatus;
    private String keyword;
    private Integer pageNum;
    private Integer pageSize;

    public Integer getClaimType() { return claimType; }
    public void setClaimType(Integer value) { claimType = value; }
    public Integer getClaimStatus() { return claimStatus; }
    public void setClaimStatus(Integer value) { claimStatus = value; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer value) { pageNum = value; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer value) { pageSize = value; }
}
