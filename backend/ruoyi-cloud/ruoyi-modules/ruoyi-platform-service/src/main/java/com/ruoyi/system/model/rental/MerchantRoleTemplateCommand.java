package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 平台维护全局商户角色模板时使用的写入命令。
 *
 * <p>该对象只描述全局角色定义，不包含租户、商户或用户归属字段，避免平台接口
 * 将角色模板错误绑定到某一个商户。</p>
 */
public class MerchantRoleTemplateCommand
{
    /** 修改时使用的角色模板主键，新增时为空。 */
    private Long roleTemplateId;

    /** 全局角色展示名称。 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称不能超过30个字符")
    private String roleName;

    /** 全局角色权限字符。 */
    @NotBlank(message = "权限字符不能为空")
    @Size(max = 100, message = "权限字符不能超过100个字符")
    private String roleKey;

    /** 全局角色显示顺序。 */
    @NotNull(message = "显示顺序不能为空")
    @PositiveOrZero(message = "显示顺序不能小于0")
    private Integer roleSort;

    /** 菜单树是否启用父子联动。 */
    private Boolean menuCheckStrictly;

    /** 角色状态，0正常，1停用。 */
    @Pattern(regexp = "^[01]$", message = "角色状态不合法")
    private String status;

    /** 角色模板拥有的商户菜单主键集合。 */
    private Long[] menuIds;

    /** 平台维护备注。 */
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    /**
     * 获取角色模板主键。
     *
     * @return 角色模板主键，新增时为空
     */
    public Long getRoleTemplateId()
    {
        return roleTemplateId;
    }

    /**
     * 设置角色模板主键。
     *
     * @param roleTemplateId 角色模板主键
     */
    public void setRoleTemplateId(Long roleTemplateId)
    {
        this.roleTemplateId = roleTemplateId;
    }

    /**
     * 获取角色名称。
     *
     * @return 角色名称
     */
    public String getRoleName()
    {
        return roleName;
    }

    /**
     * 设置角色名称。
     *
     * @param roleName 角色名称
     */
    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    /**
     * 获取权限字符。
     *
     * @return 权限字符
     */
    public String getRoleKey()
    {
        return roleKey;
    }

    /**
     * 设置权限字符。
     *
     * @param roleKey 权限字符
     */
    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    /**
     * 获取显示顺序。
     *
     * @return 显示顺序
     */
    public Integer getRoleSort()
    {
        return roleSort;
    }

    /**
     * 设置显示顺序。
     *
     * @param roleSort 显示顺序
     */
    public void setRoleSort(Integer roleSort)
    {
        this.roleSort = roleSort;
    }

    /**
     * 获取菜单树父子联动配置。
     *
     * @return true表示父子联动，false表示严格选择
     */
    public Boolean getMenuCheckStrictly()
    {
        return menuCheckStrictly;
    }

    /**
     * 设置菜单树父子联动配置。
     *
     * @param menuCheckStrictly 父子联动配置
     */
    public void setMenuCheckStrictly(Boolean menuCheckStrictly)
    {
        this.menuCheckStrictly = menuCheckStrictly;
    }

    /**
     * 获取角色状态。
     *
     * @return 0表示正常，1表示停用
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置角色状态。
     *
     * @param status 0表示正常，1表示停用
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 获取角色菜单主键集合。
     *
     * @return 角色模板菜单主键集合
     */
    public Long[] getMenuIds()
    {
        return menuIds;
    }

    /**
     * 设置角色菜单主键集合。
     *
     * @param menuIds 角色模板菜单主键集合
     */
    public void setMenuIds(Long[] menuIds)
    {
        this.menuIds = menuIds;
    }

    /**
     * 获取角色备注。
     *
     * @return 角色备注
     */
    public String getRemark()
    {
        return remark;
    }

    /**
     * 设置角色备注。
     *
     * @param remark 角色备注
     */
    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
