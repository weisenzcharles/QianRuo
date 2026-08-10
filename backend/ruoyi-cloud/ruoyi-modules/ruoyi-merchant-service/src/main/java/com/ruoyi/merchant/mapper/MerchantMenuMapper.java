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

    /**
     * 查询未通过认证商户可以预览的全部启用页面菜单。页面展示与业务操作权限分离，操作权限仍由后端鉴权控制。
     *
     * @return 全局启用的商户页面菜单
     */
    List<MerchantMenu> selectDefaultPreviewMenus();
    MerchantMenu selectById(@Param("menuId") Long menuId);
    List<Long> selectIdsByRole(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("strict") boolean strict);
    int countValidMenuIds(@Param("menuIds") Long[] menuIds);
}
