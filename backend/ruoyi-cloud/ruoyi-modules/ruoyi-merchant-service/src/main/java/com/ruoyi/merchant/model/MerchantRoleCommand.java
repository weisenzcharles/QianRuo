package com.ruoyi.merchant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 商户角色新增和修改命令。 */
public class MerchantRoleCommand
{
    private Long roleId;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "权限字符不能为空")
    private String roleKey;

    @NotNull(message = "显示顺序不能为空")
    private Integer roleSort;

    private Boolean menuCheckStrictly;
    private String status;
    private Long[] menuIds;
    private String remark;

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
    public Long[] getMenuIds() { return menuIds; }
    public void setMenuIds(Long[] menuIds) { this.menuIds = menuIds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
