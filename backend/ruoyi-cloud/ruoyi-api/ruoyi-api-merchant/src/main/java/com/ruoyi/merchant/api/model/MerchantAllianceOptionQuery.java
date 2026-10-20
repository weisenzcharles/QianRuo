package com.ruoyi.merchant.api.model;

/** 查询当前商户可转单的联盟商户。 */
public class MerchantAllianceOptionQuery
{
    private Long tenantId;
    private Long memberId;

    public MerchantAllianceOptionQuery() { }

    public MerchantAllianceOptionQuery(Long tenantId, Long memberId)
    {
        this.tenantId = tenantId;
        this.memberId = memberId;
    }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
}
