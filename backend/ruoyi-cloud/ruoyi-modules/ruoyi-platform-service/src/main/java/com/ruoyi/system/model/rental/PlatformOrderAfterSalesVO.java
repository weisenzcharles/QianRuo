package com.ruoyi.system.model.rental;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/** 平台订单详情中的售后案件浏览对象。 */
public class PlatformOrderAfterSalesVO
{
    private String caseNo;
    private String caseTypeName;
    private String caseStatusName;
    private String problemDesc;
    private BigDecimal compensationAmount;
    private String createTime;
    private String finishTime;
    private String remark;
    private List<PlatformOrderAfterSalesEventVO> events = Collections.emptyList();
    private List<PlatformOrderAfterSalesEvidenceVO> evidence = Collections.emptyList();

    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String value) { caseNo = value; }
    public String getCaseTypeName() { return caseTypeName; }
    public void setCaseTypeName(String value) { caseTypeName = value; }
    public String getCaseStatusName() { return caseStatusName; }
    public void setCaseStatusName(String value) { caseStatusName = value; }
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String value) { problemDesc = value; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal value) { compensationAmount = value; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
    public String getFinishTime() { return finishTime; }
    public void setFinishTime(String value) { finishTime = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
    public List<PlatformOrderAfterSalesEventVO> getEvents() { return events; }
    public void setEvents(List<PlatformOrderAfterSalesEventVO> value) { events = value == null ? Collections.emptyList() : value; }
    public List<PlatformOrderAfterSalesEvidenceVO> getEvidence() { return evidence; }
    public void setEvidence(List<PlatformOrderAfterSalesEvidenceVO> value) { evidence = value == null ? Collections.emptyList() : value; }
}
