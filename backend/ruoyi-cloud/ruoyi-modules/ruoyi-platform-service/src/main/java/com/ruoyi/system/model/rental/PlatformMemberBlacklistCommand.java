package com.ruoyi.system.model.rental;

/** 平台新增商户黑名单记录命令。 */
public class PlatformMemberBlacklistCommand
{
    private Integer reasonType;
    private String reason;
    private String expiresTime;

    public Integer getReasonType()
    {
        return reasonType;
    }

    public void setReasonType(Integer reasonType)
    {
        this.reasonType = reasonType;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getExpiresTime()
    {
        return expiresTime;
    }

    public void setExpiresTime(String expiresTime)
    {
        this.expiresTime = expiresTime;
    }
}
