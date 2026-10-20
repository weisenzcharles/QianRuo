package com.ruoyi.system.api.model;

/** 商户可见的信用流水视图，不暴露平台数据库主键。 */
public class MerchantCreditRecordView
{
    private String recordNo;
    private Integer eventType;
    private String eventTypeName;
    private Integer scoreBefore;
    private Integer scoreChange;
    private Integer scoreAfter;
    private String reason;
    private String sourceNo;
    private String reversalOfRecordNo;
    private String reversalRecordNo;
    private String createTime;

    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public Integer getEventType() { return eventType; }
    public void setEventType(Integer eventType) { this.eventType = eventType; }
    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String eventTypeName) { this.eventTypeName = eventTypeName; }
    public Integer getScoreBefore() { return scoreBefore; }
    public void setScoreBefore(Integer scoreBefore) { this.scoreBefore = scoreBefore; }
    public Integer getScoreChange() { return scoreChange; }
    public void setScoreChange(Integer scoreChange) { this.scoreChange = scoreChange; }
    public Integer getScoreAfter() { return scoreAfter; }
    public void setScoreAfter(Integer scoreAfter) { this.scoreAfter = scoreAfter; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public String getReversalOfRecordNo() { return reversalOfRecordNo; }
    public void setReversalOfRecordNo(String reversalOfRecordNo) { this.reversalOfRecordNo = reversalOfRecordNo; }
    public String getReversalRecordNo() { return reversalRecordNo; }
    public void setReversalRecordNo(String reversalRecordNo) { this.reversalRecordNo = reversalRecordNo; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
