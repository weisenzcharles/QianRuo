package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysRoleMenu;

/**
 * 商户端角色菜单关联 数据层
 *
 * @author qianruo
 */
public interface RentalMerchantRoleMenuMapper
{
    public int checkMenuExistRole(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("menuId") Long menuId);

    public int deleteRoleMenuByRoleId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleId") Long roleId);

    public int deleteRoleMenu(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleIds") Long[] roleIds);

    public int batchRoleMenu(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("list") List<SysRoleMenu> roleMenuList);
}
