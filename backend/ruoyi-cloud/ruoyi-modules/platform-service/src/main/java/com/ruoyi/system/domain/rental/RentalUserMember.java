package com.ruoyi.system.domain.rental;

import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 商户员工绑定 ry_rental_user_member
 *
 * @author qianruo
 */
public class RentalUserMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private Long memberId;

    private Long merchantUserId;

    private String memberRole;

    private String warehouseScopeType;

    private String sensitiveScope;

    private String status;

    private String delFlag;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

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

    public Long getMerchantUserId()
    {
        return merchantUserId;
    }

    public void setMerchantUserId(Long merchantUserId)
    {
        this.merchantUserId = merchantUserId;
    }

    public String getMemberRole()
    {
        return memberRole;
    }

    public void setMemberRole(String memberRole)
    {
        this.memberRole = memberRole;
    }

    public String getWarehouseScopeType()
    {
        return warehouseScopeType;
    }

    public void setWarehouseScopeType(String warehouseScopeType)
    {
        this.warehouseScopeType = warehouseScopeType;
    }

    public String getSensitiveScope()
    {
        return sensitiveScope;
    }

    public void setSensitiveScope(String sensitiveScope)
    {
        this.sensitiveScope = sensitiveScope;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
