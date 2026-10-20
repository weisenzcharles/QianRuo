package com.ruoyi.system.domain.rental;

import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 仓库基础信息。
 *
 * @author qianruo
 */
public class RentalWarehouse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long warehouseId;

    private Long tenantId;

    private Long memberId;

    private String warehouseCode;

    private String warehouseName;

    private Integer status;

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
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

    public String getWarehouseCode()
    {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode)
    {
        this.warehouseCode = warehouseCode;
    }

    public String getWarehouseName()
    {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }
}
