package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.domain.SysRole;

/**
 * 商户端角色 数据层
 *
 * @author qianruo
 */
public interface RentalMerchantRoleMapper
{
    public List<SysRole> selectRoleList(SysRole role);

    public List<SysRole> selectRolePermissionByUserId(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("merchantUserId") Long merchantUserId);

    public List<Long> selectRoleListByUserId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    public SysRole selectRoleById(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleId") Long roleId);

    public SysRole checkRoleNameUnique(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleName") String roleName);

    public SysRole checkRoleKeyUnique(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleKey") String roleKey);

    public int insertRole(SysRole role);

    public int updateRole(SysRole role);

    public int deleteRoleById(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleId") Long roleId);

    public int deleteRoleByIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleIds") Long[] roleIds);
}
