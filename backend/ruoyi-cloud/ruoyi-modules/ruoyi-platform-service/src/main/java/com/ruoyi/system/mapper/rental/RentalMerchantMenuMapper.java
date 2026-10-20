package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysMenu;

/**
 * 商户端菜单 数据层
 *
 * @author qianruo
 */
public interface RentalMerchantMenuMapper
{
    public List<SysMenu> selectMenuList(SysMenu menu);

    public List<SysMenu> selectMenuTreeAll(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    public List<SysMenu> selectMenuTreeByUserId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    public List<Long> selectMenuListByRoleId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

    public List<String> selectMenuPermsByUserId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    public List<String> selectMenuPermsByRoleId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleId") Long roleId);

    public SysMenu selectMenuById(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("menuId") Long menuId);

    public int hasChildByMenuId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("menuId") Long menuId);

    public int checkMenuExistRole(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("menuId") Long menuId);

    public SysMenu checkMenuNameUnique(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("menuName") String menuName, @Param("parentId") Long parentId);

    public List<SysMenu> selectMenusByPathOrRouteName(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("path") String path, @Param("routeName") String routeName);

    public int insertMenu(SysMenu menu);

    public int updateMenu(SysMenu menu);

    public void updateMenuSort(SysMenu menu);

    public int deleteMenuById(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("menuId") Long menuId);
}
