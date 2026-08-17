package com.ruoyi.system.model.rental;

import java.util.Date;

/**
 * 平台全局商户角色模板返回对象。
 *
 * <p>对象不暴露商户归属字段，避免前端误以为角色模板属于某个商户。</p>
 */
public class MerchantRoleTemplateVO
{
    /** 角色模板主键。 */
    private Long roleTemplateId;

    /** 角色名称。 */
    private String roleName;

    /** 权限字符。 */
    private String roleKey;

    /** 显示顺序。 */
    private Integer roleSort;

    /** 菜单树父子联动配置。 */
    private Boolean menuCheckStrictly;

    /** 角色状态。 */
    private String status;

    /** 软删除标识。 */
    private Integer isDelete;

    /** 创建人。 */
    private String createBy;

    /** 创建时间。 */
    private Date createTime;

    /** 修改人。 */
    private String updateBy;

    /** 修改时间。 */
    private Date updateTime;

    /** 备注。 */
    private String remark;

    /** 已授权菜单主键集合。 */
    private Long[] menuIds;

    /** @return 角色模板主键 */
    public Long getRoleTemplateId() { return roleTemplateId; }
    /** @param roleTemplateId 角色模板主键 */
    public void setRoleTemplateId(Long roleTemplateId) { this.roleTemplateId = roleTemplateId; }
    /** @return 角色名称 */
    public String getRoleName() { return roleName; }
    /** @param roleName 角色名称 */
    public void setRoleName(String roleName) { this.roleName = roleName; }
    /** @return 权限字符 */
    public String getRoleKey() { return roleKey; }
    /** @param roleKey 权限字符 */
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
    /** @return 显示顺序 */
    public Integer getRoleSort() { return roleSort; }
    /** @param roleSort 显示顺序 */
    public void setRoleSort(Integer roleSort) { this.roleSort = roleSort; }
    /** @return 菜单树父子联动配置 */
    public Boolean getMenuCheckStrictly() { return menuCheckStrictly; }
    /** @param menuCheckStrictly 菜单树父子联动配置 */
    public void setMenuCheckStrictly(Boolean menuCheckStrictly) { this.menuCheckStrictly = menuCheckStrictly; }
    /** @return 角色状态 */
    public String getStatus() { return status; }
    /** @param status 角色状态 */
    public void setStatus(String status) { this.status = status; }
    /** @return 软删除标识 */
    public Integer getIsDelete() { return isDelete; }
    /** @param isDelete 软删除标识 */
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    /** @return 创建人 */
    public String getCreateBy() { return createBy; }
    /** @param createBy 创建人 */
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    /** @return 创建时间 */
    public Date getCreateTime() { return createTime; }
    /** @param createTime 创建时间 */
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    /** @return 修改人 */
    public String getUpdateBy() { return updateBy; }
    /** @param updateBy 修改人 */
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    /** @return 修改时间 */
    public Date getUpdateTime() { return updateTime; }
    /** @param updateTime 修改时间 */
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    /** @return 备注 */
    public String getRemark() { return remark; }
    /** @param remark 备注 */
    public void setRemark(String remark) { this.remark = remark; }
    /** @return 已授权菜单主键集合 */
    public Long[] getMenuIds() { return menuIds; }
    /** @param menuIds 已授权菜单主键集合 */
    public void setMenuIds(Long[] menuIds) { this.menuIds = menuIds; }
}
