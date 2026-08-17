package com.ruoyi.system.model.rental;

import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 平台全局商户角色模板查询条件。
 *
 * <p>查询对象不提供 memberId、tenantId 等归属条件，角色模板天然属于平台全局命名空间。</p>
 */
public class MerchantRoleTemplateQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 按角色名称模糊查询。 */
    private String roleName;

    /** 按权限字符模糊查询。 */
    private String roleKey;

    /** 按角色状态查询。 */
    private String status;

    /**
     * 获取角色名称查询条件。
     *
     * @return 角色名称查询条件
     */
    public String getRoleName()
    {
        return roleName;
    }

    /**
     * 设置角色名称查询条件。
     *
     * @param roleName 角色名称查询条件
     */
    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    /**
     * 获取权限字符查询条件。
     *
     * @return 权限字符查询条件
     */
    public String getRoleKey()
    {
        return roleKey;
    }

    /**
     * 设置权限字符查询条件。
     *
     * @param roleKey 权限字符查询条件
     */
    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    /**
     * 获取角色状态查询条件。
     *
     * @return 角色状态查询条件
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置角色状态查询条件。
     *
     * @param status 0表示正常，1表示停用
     */
    public void setStatus(String status)
    {
        this.status = status;
    }
}
