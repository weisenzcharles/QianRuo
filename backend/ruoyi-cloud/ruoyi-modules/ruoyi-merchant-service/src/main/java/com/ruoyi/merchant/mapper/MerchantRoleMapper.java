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
            @Param("command") MerchantRoleCommand command, @Param("operator") Long operator);
    int update(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantRoleCommand command, @Param("operator") Long operator);
    int updateStatus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("status") Integer status, @Param("operator") Long operator);
    int softDelete(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleIds") Long[] roleIds, @Param("operator") Long operator);
    int countUsers(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);
    List<Long> selectUserIdsByRole(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);
    /** 查询当前商户用户已分配的角色。 */
    List<Long> selectRoleIdsByUser(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId);
    /** 统计属于当前商户的角色数量。 */
    int countRolesInMerchant(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleIds") Long[] roleIds);
    /** 统计提交角色中是否包含商户管理员角色。 */
    int countAdminRoles(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleIds") Long[] roleIds);
    /** 删除用户的指定角色关系。 */
    int deleteUserRoleBatch(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleIds") Long[] roleIds, @Param("userIds") Long[] userIds);
    /** 写入用户角色关系。 */
    int insertUserRoles(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("roleIds") Long[] roleIds);
    int deleteRoleMenus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);
    int insertRoleMenus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("menuIds") Long[] menuIds);
    /**
     * 为当前商户补齐平台新增的全局角色模板。
     *
     * @param tenantId 当前商户租户唯一编号
     * @param merchantId 当前商户唯一编号
     * @param operator 当前操作人
     * @return 新增角色数量
     */
    int insertMissingTemplateRoles(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("operator") Long operator);
    /**
     * 将当前商户角色模板的默认菜单权限幂等写入运行角色。
     *
     * @param tenantId 当前商户租户唯一编号
     * @param merchantId 当前商户唯一编号
     * @return 新增或恢复的角色菜单数量
     */
    int insertTemplateRoleMenus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId);
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
