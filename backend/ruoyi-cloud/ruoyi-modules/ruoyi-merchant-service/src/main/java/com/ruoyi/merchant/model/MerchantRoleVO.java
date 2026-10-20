package com.ruoyi.merchant.model;

import java.util.Date;

/** 商户角色返回视图。 */
public class MerchantRoleVO
{
    private Long roleId;
    private String roleName;
    private String roleKey;
    private Integer roleSort;
    private Integer menuCheckStrictly;
    private Integer status;
    private Integer isDelete;
    private String remark;
    private Date createTime;
    private Long[] menuIds;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
    public Integer getRoleSort() { return roleSort; }
    public void setRoleSort(Integer roleSort) { this.roleSort = roleSort; }
    public Integer getMenuCheckStrictly() { return menuCheckStrictly; }
    public void setMenuCheckStrictly(Integer menuCheckStrictly) { this.menuCheckStrictly = menuCheckStrictly; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Long[] getMenuIds() { return menuIds; }
    public void setMenuIds(Long[] menuIds) { this.menuIds = menuIds; }
}
