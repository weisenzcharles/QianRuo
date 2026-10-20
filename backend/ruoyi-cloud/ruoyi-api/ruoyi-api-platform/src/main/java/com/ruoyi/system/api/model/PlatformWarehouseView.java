package com.ruoyi.system.api.model;

import java.io.Serializable;

/**
 * 平台服务读取租赁业务服务仓库数据时使用的传输对象。
 *
 * <p>该对象只承载跨服务契约字段，不暴露租赁业务服务的数据库实体。</p>
 */
public class PlatformWarehouseView implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 仓库唯一编号。 */
    private Long warehouseId;

    /** 租户唯一编号。 */
    private Long tenantId;

    /** 商户唯一编号。 */
    private Long memberId;

    /** 仓库编码。 */
    private String warehouseCode;

    /** 仓库名称。 */
    private String warehouseName;

    /** 仓库状态。 */
    private Integer status;

    /** 获取仓库唯一编号。 */
    public Long getWarehouseId()
    {
        return warehouseId;
    }

    /** 设置仓库唯一编号。 */
    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    /** 获取租户唯一编号。 */
    public Long getTenantId()
    {
        return tenantId;
    }

    /** 设置租户唯一编号。 */
    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    /** 获取商户唯一编号。 */
    public Long getMemberId()
    {
        return memberId;
    }

    /** 设置商户唯一编号。 */
    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    /** 获取仓库编码。 */
    public String getWarehouseCode()
    {
        return warehouseCode;
    }

    /** 设置仓库编码。 */
    public void setWarehouseCode(String warehouseCode)
    {
        this.warehouseCode = warehouseCode;
    }

    /** 获取仓库名称。 */
    public String getWarehouseName()
    {
        return warehouseName;
    }

    /** 设置仓库名称。 */
    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
    }

    /** 获取仓库状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置仓库状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }
}
