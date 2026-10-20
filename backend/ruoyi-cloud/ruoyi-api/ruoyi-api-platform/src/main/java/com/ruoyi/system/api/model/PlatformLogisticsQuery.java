package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台物流治理分页筛选条件。 */
public class PlatformLogisticsQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String keyword;
    private String merchantKeyword;
    private Integer direction;
    private Integer routeStatus;
    private Boolean abnormalOnly;
    private Integer pageNum;
    private Integer pageSize;

    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public String getMerchantKeyword() { return merchantKeyword; }
    public void setMerchantKeyword(String value) { merchantKeyword = value; }
    public Integer getDirection() { return direction; }
    public void setDirection(Integer value) { direction = value; }
    public Integer getRouteStatus() { return routeStatus; }
    public void setRouteStatus(Integer value) { routeStatus = value; }
    public Boolean getAbnormalOnly() { return abnormalOnly; }
    public void setAbnormalOnly(Boolean value) { abnormalOnly = value; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer value) { pageNum = value; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer value) { pageSize = value; }
}
