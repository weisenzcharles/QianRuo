package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantRoleCommand;
import com.ruoyi.merchant.model.MerchantRoleQuery;
import com.ruoyi.merchant.model.MerchantRoleUserQuery;
import com.ruoyi.merchant.model.MerchantRoleUserVO;
import com.ruoyi.merchant.model.MerchantRoleVO;

/** 商户角色及授权关系数据访问。 */
public interface MerchantRoleMapper
{
    List<MerchantRoleVO> selectList(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("query") MerchantRoleQuery query);
    MerchantRoleVO selectById(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);
    MerchantRoleVO selectByName(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleName") String roleName);
    MerchantRoleVO selectByKey(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleKey") String roleKey);
    int insert(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantRoleCommand command, @Param("operator") String operator);
    int update(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantRoleCommand command, @Param("operator") String operator);
    int updateStatus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("status") String status, @Param("operator") String operator);
    int softDelete(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleIds") Long[] roleIds, @Param("operator") String operator);
    int countUsers(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);
    List<Long> selectUserIdsByRole(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);
    int deleteRoleMenus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);
    int insertRoleMenus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("menuIds") Long[] menuIds);
    List<MerchantRoleUserVO> selectAllocatedUsers(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantRoleUserQuery query);
    List<MerchantRoleUserVO> selectUnallocatedUsers(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantRoleUserQuery query);
    int countUsersInMerchant(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userIds") Long[] userIds);
    int deleteUserRole(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
    int insertUserRole(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
}
