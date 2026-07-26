package com.ruoyi.merchant.model;

import java.util.Date;

/** 商户角色返回视图。 */
public class MerchantRoleVO
{
    private Long roleId;
    private String roleName;
    private String roleKey;
    private Integer roleSort;
    private Boolean menuCheckStrictly;
    private String status;
    private String delFlag;
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
    public Boolean getMenuCheckStrictly() { return menuCheckStrictly; }
    public void setMenuCheckStrictly(Boolean menuCheckStrictly) { this.menuCheckStrictly = menuCheckStrictly; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Long[] getMenuIds() { return menuIds; }
    public void setMenuIds(Long[] menuIds) { this.menuIds = menuIds; }
}
