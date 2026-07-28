package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysRoleMenu;

/**
 * 商户端用户菜单关联 数据层
 *
 * @author qianruo
 */
public interface RentalMerchantUserMenuMapper
{
    public List<Long> selectMenuIdsByUserId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

    public int deleteUserMenuByUserIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserIds") Long[] merchantUserIds);

    public int batchUserMenu(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("list") List<SysRoleMenu> userMenuList);
}
