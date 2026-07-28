package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalWarehouse;

/**
 * 仓库基础信息 数据层。
 *
 * @author qianruo
 */
public interface RentalWarehouseMapper
{
    public List<RentalWarehouse> selectEnabledByMember(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    public int countEnabledByIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseIds") List<Long> warehouseIds);
}
