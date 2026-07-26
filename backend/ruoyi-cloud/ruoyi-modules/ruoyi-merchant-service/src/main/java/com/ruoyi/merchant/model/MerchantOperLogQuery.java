package com.ruoyi.merchant.model;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户操作日志查询条件。租户和商户归属由服务端补充。 */
public class MerchantOperLogQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String operIp;
    private String title;
    private String operName;
    private Integer businessType;
    private Integer status;

    public String getOperIp() { return operIp; }
    public void setOperIp(String operIp) { this.operIp = operIp; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOperName() { return operName; }
    public void setOperName(String operName) { this.operName = operName; }
    public Integer getBusinessType() { return businessType; }
    public void setBusinessType(Integer businessType) { this.businessType = businessType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
