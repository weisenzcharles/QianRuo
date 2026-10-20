package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/** 平台订单详情中的归还验机浏览对象。 */
public class PlatformOrderReturnInspectionVO
{
    private String resultLabel;
    private String caseTypeLabel;
    private String problemDesc;
    private String lostItemsJson;
    private BigDecimal compensationAmount;
    private String customerContactedLabel;
    private String remark;
    private String inspectedTime;

    public String getResultLabel() { return resultLabel; }
    public void setResultLabel(String value) { resultLabel = value; }
    public String getCaseTypeLabel() { return caseTypeLabel; }
    public void setCaseTypeLabel(String value) { caseTypeLabel = value; }
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String value) { problemDesc = value; }
    public String getLostItemsJson() { return lostItemsJson; }
    public void setLostItemsJson(String value) { lostItemsJson = value; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal value) { compensationAmount = value; }
    public String getCustomerContactedLabel() { return customerContactedLabel; }
    public void setCustomerContactedLabel(String value) { customerContactedLabel = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
    public String getInspectedTime() { return inspectedTime; }
    public void setInspectedTime(String value) { inspectedTime = value; }
}
