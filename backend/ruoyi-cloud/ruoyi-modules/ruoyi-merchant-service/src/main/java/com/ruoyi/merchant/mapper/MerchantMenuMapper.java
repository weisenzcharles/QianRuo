package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.domain.MerchantMenu;

/** 商户菜单只读数据访问。 */
public interface MerchantMenuMapper
{
    List<MerchantMenu> selectCatalog();
    List<MerchantMenu> selectByUser(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId);
    MerchantMenu selectById(@Param("menuId") Long menuId);
    List<Long> selectIdsByRole(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("strict") boolean strict);
    int countValidMenuIds(@Param("menuIds") Long[] menuIds);
}
