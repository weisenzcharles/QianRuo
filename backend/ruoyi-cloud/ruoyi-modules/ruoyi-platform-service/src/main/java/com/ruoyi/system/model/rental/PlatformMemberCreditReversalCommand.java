package com.ruoyi.system.model.rental;

/** 平台对已确认信用流水执行申诉冲正的命令。原流水不会被修改。 */
public class PlatformMemberCreditReversalCommand
{
    private String reason;

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
