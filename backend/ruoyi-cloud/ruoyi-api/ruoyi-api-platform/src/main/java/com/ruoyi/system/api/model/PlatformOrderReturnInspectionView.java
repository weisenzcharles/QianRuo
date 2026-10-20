package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台订单详情中的最近一次归还验机记录。 */
public class PlatformOrderReturnInspectionView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer result;
    private String resultLabel;
    private Integer caseType;
    private String caseTypeLabel;
    private String problemDesc;
    private String lostItemsJson;
    private BigDecimal compensationAmount;
    private Integer customerContacted;
    private String remark;
    private String inspectedTime;

    public Integer getResult() { return result; }
    public void setResult(Integer value) { result = value; }
    public String getResultLabel() { return resultLabel; }
    public void setResultLabel(String value) { resultLabel = value; }
    public Integer getCaseType() { return caseType; }
    public void setCaseType(Integer value) { caseType = value; }
    public String getCaseTypeLabel() { return caseTypeLabel; }
    public void setCaseTypeLabel(String value) { caseTypeLabel = value; }
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String value) { problemDesc = value; }
    public String getLostItemsJson() { return lostItemsJson; }
    public void setLostItemsJson(String value) { lostItemsJson = value; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal value) { compensationAmount = value; }
    public Integer getCustomerContacted() { return customerContacted; }
    public void setCustomerContacted(Integer value) { customerContacted = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
    public String getInspectedTime() { return inspectedTime; }
    public void setInspectedTime(String value) { inspectedTime = value; }
}
