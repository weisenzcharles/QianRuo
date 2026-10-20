package com.ruoyi.system.model.rental;

/** 平台根据商户编码调用租赁业务明细所需的内部归属范围。 */
public class PlatformMerchantBusinessScope
{
    private Long tenantId;
    private Long memberId;
    private String memberCode;
    private String merchantName;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
}
