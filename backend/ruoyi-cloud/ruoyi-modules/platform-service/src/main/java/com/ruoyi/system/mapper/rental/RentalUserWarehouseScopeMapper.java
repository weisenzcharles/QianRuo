package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 商户用户仓库权限 数据层。
 *
 * @author qianruo
 */
public interface RentalUserWarehouseScopeMapper
{
    public List<Long> selectWarehouseIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    public int batchInsert(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("warehouseIds") List<Long> warehouseIds,
            @Param("createBy") String createBy);

    public int deleteByUser(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);
}
