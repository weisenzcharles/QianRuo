package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.MerchantRoleTemplateCommand;
import com.ruoyi.system.model.rental.MerchantRoleTemplateQuery;
import com.ruoyi.system.model.rental.MerchantRoleTemplateVO;

/** 平台全局商户角色模板及菜单授权数据访问。 */
public interface RentalMerchantRoleTemplateMapper
{
    /**
     * 分页查询平台全局角色模板。
     *
     * @param query 角色名称、权限字符和状态查询条件
     * @return 全局角色模板列表
     */
    List<MerchantRoleTemplateVO> selectList(@Param("query") MerchantRoleTemplateQuery query);

    /**
     * 按主键查询全局角色模板。
     *
     * @param roleTemplateId 角色模板主键
     * @return 角色模板，不存在时返回 null
     */
    MerchantRoleTemplateVO selectById(@Param("roleTemplateId") Long roleTemplateId);

    /**
     * 按名称查询未删除的全局角色模板。
     *
     * @param roleName 角色名称
     * @return 重名角色模板，不存在时返回 null
     */
    MerchantRoleTemplateVO selectByName(@Param("roleName") String roleName);

    /**
     * 按权限字符查询未删除的全局角色模板。
     *
     * @param roleKey 权限字符
     * @return 重复权限字符的角色模板，不存在时返回 null
     */
    MerchantRoleTemplateVO selectByKey(@Param("roleKey") String roleKey);

    /**
     * 新增全局角色模板并回写数据库生成的主键。
     *
     * @param command 全局角色模板写入命令
     * @param operator 平台操作人
     * @return 新增行数
     */
    int insert(@Param("command") MerchantRoleTemplateCommand command, @Param("operator") String operator);

    /**
     * 修改全局角色模板基础信息。
     *
     * @param command 全局角色模板写入命令
     * @param operator 平台操作人
     * @return 修改行数
     */
    int update(@Param("command") MerchantRoleTemplateCommand command, @Param("operator") String operator);

    /**
     * 软删除全局角色模板。
     *
     * @param roleTemplateIds 待删除角色模板主键集合
     * @param operator 平台操作人
     * @return 修改行数
     */
    int softDelete(@Param("roleTemplateIds") Long[] roleTemplateIds, @Param("operator") String operator);

    /**
     * 查询角色模板已经选中的菜单主键。
     *
     * @param roleTemplateId 角色模板主键
     * @return 菜单主键集合
     */
    List<Long> selectMenuIds(@Param("roleTemplateId") Long roleTemplateId);

    /**
     * 软删除角色模板全部菜单授权。
     *
     * @param roleTemplateId 角色模板主键
     * @return 修改行数
     */
    int deleteMenus(@Param("roleTemplateId") Long roleTemplateId);

    /**
     * 批量恢复或新增角色模板菜单授权。
     *
     * @param roleTemplateId 角色模板主键
     * @param menuIds 菜单主键集合
     * @return 新增或恢复行数
     */
    int insertMenus(@Param("roleTemplateId") Long roleTemplateId, @Param("menuIds") Long[] menuIds);
}
