package com.ruoyi.system.model.rental;

/** 平台风险服务读取的商户归属范围。 */
public class PlatformMemberRiskScope
{
    private Long tenantId;
    private Long memberId;

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }
}
