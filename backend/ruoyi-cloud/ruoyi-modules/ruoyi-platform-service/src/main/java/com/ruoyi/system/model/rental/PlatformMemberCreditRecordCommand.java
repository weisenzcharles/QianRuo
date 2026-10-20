package com.ruoyi.system.model.rental;

/** 平台新增商户信用变更记录命令。商户归属由路径中的商户唯一编号确定。 */
public class PlatformMemberCreditRecordCommand
{
    private Integer eventType;
    private Integer scoreChange;
    private String reason;
    private String sourceNo;

    public Integer getEventType()
    {
        return eventType;
    }

    public void setEventType(Integer eventType)
    {
        this.eventType = eventType;
    }

    public Integer getScoreChange()
    {
        return scoreChange;
    }

    public void setScoreChange(Integer scoreChange)
    {
        this.scoreChange = scoreChange;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getSourceNo()
    {
        return sourceNo;
    }

    public void setSourceNo(String sourceNo)
    {
        this.sourceNo = sourceNo;
    }
}
