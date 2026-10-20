package com.ruoyi.rentalbusiness.model;

import java.util.LinkedHashSet;
import java.util.Set;

/** 商户物流治理列表筛选条件，仓库范围由服务端登录上下文覆盖。 */
public class MerchantLogisticsQuery
{
    private String keyword;
    private Integer direction;
    private Integer routeStatus;
    private boolean abnormalOnly;
    private boolean allWarehouseAccess;
    private Set<Long> warehouseIds = new LinkedHashSet<Long>();

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public Integer getDirection()
    {
        return direction;
    }

    public void setDirection(Integer direction)
    {
        this.direction = direction;
    }

    public Integer getRouteStatus()
    {
        return routeStatus;
    }

    public void setRouteStatus(Integer routeStatus)
    {
        this.routeStatus = routeStatus;
    }

    public boolean isAbnormalOnly()
    {
        return abnormalOnly;
    }

    public void setAbnormalOnly(boolean abnormalOnly)
    {
        this.abnormalOnly = abnormalOnly;
    }

    public boolean isAllWarehouseAccess()
    {
        return allWarehouseAccess;
    }

    public void setAllWarehouseAccess(boolean allWarehouseAccess)
    {
        this.allWarehouseAccess = allWarehouseAccess;
    }

    public Set<Long> getWarehouseIds()
    {
        return warehouseIds;
    }

    public void setWarehouseIds(Set<Long> warehouseIds)
    {
        this.warehouseIds = warehouseIds == null
                ? new LinkedHashSet<Long>()
                : warehouseIds;
    }
}
