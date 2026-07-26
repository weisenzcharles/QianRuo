package com.ruoyi.merchant.model;

/** 商户角色状态命令。 */
public class MerchantRoleStatusCommand
{
    private Long roleId;
    private String status;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
