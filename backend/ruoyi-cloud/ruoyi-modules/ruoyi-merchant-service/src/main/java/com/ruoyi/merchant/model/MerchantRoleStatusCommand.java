package com.ruoyi.merchant.model;

/** 商户角色状态命令。 */
public class MerchantRoleStatusCommand
{
    private Long roleId;
    private Integer status;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
