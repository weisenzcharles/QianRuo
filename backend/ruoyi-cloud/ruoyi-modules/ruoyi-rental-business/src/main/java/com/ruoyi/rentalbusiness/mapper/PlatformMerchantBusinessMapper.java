package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformMerchantInventoryView;
import com.ruoyi.system.api.model.PlatformMerchantGovernanceView;
import com.ruoyi.system.api.model.PlatformMerchantPackageView;
import com.ruoyi.system.api.model.PlatformMerchantProductView;
import com.ruoyi.system.api.model.PlatformMerchantWarehouseView;

/** 平台查看商户经营明细的数据访问组件。 */
public interface PlatformMerchantBusinessMapper
{
    /** 查询商户仓库明细。 */
    List<PlatformMerchantWarehouseView> selectWarehouses(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("limit") int limit);

    /** 查询商户商品目录明细。 */
    List<PlatformMerchantProductView> selectProducts(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("limit") int limit);

    /** 查询商户套餐及当前生效版本明细。 */
    List<PlatformMerchantPackageView> selectPackages(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("limit") int limit);

    /** 查询商户库存汇总明细。 */
    List<PlatformMerchantInventoryView> selectInventories(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("limit") int limit);

    /** 查询商户履约、物流和风险治理汇总。 */
    PlatformMerchantGovernanceView selectGovernance(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);
}
