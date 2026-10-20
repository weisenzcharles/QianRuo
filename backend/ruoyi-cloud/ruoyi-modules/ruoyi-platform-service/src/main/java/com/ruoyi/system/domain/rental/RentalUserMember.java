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

    private Integer warehouseScopeType;

    private Integer sensitiveScope;

    private Integer status;

    private Integer isDelete;

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

    public Integer getWarehouseScopeType()
    {
        return warehouseScopeType;
    }

    public void setWarehouseScopeType(Integer warehouseScopeType)
    {
        this.warehouseScopeType = warehouseScopeType;
    }

    public Integer getSensitiveScope()
    {
        return sensitiveScope;
    }

    public void setSensitiveScope(Integer sensitiveScope)
    {
        this.sensitiveScope = sensitiveScope;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getIsDelete()
    {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }
}
