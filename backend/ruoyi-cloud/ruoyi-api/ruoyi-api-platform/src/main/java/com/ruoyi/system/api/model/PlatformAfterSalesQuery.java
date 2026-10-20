package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台售后案件查询条件。 */
public class PlatformAfterSalesQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer caseStatus;
    private String keyword;
    private String merchantKeyword;
    private Integer pageNum;
    private Integer pageSize;

    public Integer getCaseStatus() { return caseStatus; }
    public void setCaseStatus(Integer value) { caseStatus = value; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public String getMerchantKeyword() { return merchantKeyword; }
    public void setMerchantKeyword(String value) { merchantKeyword = value; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer value) { pageNum = value; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer value) { pageSize = value; }
}
