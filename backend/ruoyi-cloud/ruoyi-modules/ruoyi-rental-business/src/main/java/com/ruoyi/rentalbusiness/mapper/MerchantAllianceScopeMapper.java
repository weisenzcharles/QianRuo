package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformAllianceScopeView;

/** 联盟共享仓库和商品范围数据访问。 */
public interface MerchantAllianceScopeMapper
{
    List<PlatformAllianceScopeView> selectPlatformScopes(@Param("tenantId") Long tenantId,
            @Param("allianceId") Long allianceId, @Param("ownerMemberId") Long ownerMemberId);
    List<Map<String, Object>> selectWarehouseScopes(@Param("tenantId") Long tenantId,
            @Param("allianceId") Long allianceId, @Param("ownerMemberId") Long ownerMemberId);

    List<Map<String, Object>> selectProductScopes(@Param("tenantId") Long tenantId,
            @Param("allianceId") Long allianceId, @Param("ownerMemberId") Long ownerMemberId);

    int upsertWarehouseScope(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("ownerMemberId") Long ownerMemberId, @Param("warehouseId") Long warehouseId,
            @Param("acceptOrderEnabled") Integer acceptOrderEnabled, @Param("shareStatus") Integer shareStatus,
            @Param("remark") String remark, @Param("operatorId") Long operatorId);

    int upsertProductScope(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("ownerMemberId") Long ownerMemberId, @Param("productId") Long productId,
            @Param("shareQuantityRatio") java.math.BigDecimal shareQuantityRatio,
            @Param("shareQuantityLimit") Integer shareQuantityLimit,
            @Param("acceptOrderEnabled") Integer acceptOrderEnabled, @Param("shareStatus") Integer shareStatus,
            @Param("remark") String remark, @Param("operatorId") Long operatorId);

    int deleteWarehouseScope(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("ownerMemberId") Long ownerMemberId, @Param("scopeId") Long scopeId,
            @Param("operatorId") Long operatorId);

    int deleteProductScope(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("ownerMemberId") Long ownerMemberId, @Param("scopeId") Long scopeId,
            @Param("operatorId") Long operatorId);
}
