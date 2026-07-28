package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysUserRole;

/**
 * 商户端用户角色关联 数据层
 *
 * @author qianruo
 */
public interface RentalMerchantUserRoleMapper
{
    public int countUserRoleByRoleId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleId") Long roleId);

    public int deleteUserRoleInfo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("userRole") SysUserRole userRole);

    public int deleteUserRoleInfos(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleId") Long roleId, @Param("userIds") Long[] userIds);

    public int deleteUserRoleByUserIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserIds") Long[] merchantUserIds);

    public int batchUserRole(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("list") List<SysUserRole> userRoleList);
}
