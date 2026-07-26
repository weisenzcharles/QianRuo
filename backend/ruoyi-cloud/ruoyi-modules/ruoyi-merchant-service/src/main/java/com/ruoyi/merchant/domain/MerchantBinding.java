package com.ruoyi.merchant.domain;

/** 商户账号与商户主体的有效绑定。 */
public class MerchantBinding
{
    private Long tenantId;
    private Long merchantId;
    private Long userId;
    private String memberRole;
    private String warehouseScopeType;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }
    public String getWarehouseScopeType() { return warehouseScopeType; }
    public void setWarehouseScopeType(String warehouseScopeType) { this.warehouseScopeType = warehouseScopeType; }
}
