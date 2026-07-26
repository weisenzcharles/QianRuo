package com.ruoyi.merchant.model;

/** 单个商户用户角色授权命令。 */
public class MerchantUserRoleCommand
{
    private Long userId;
    private Long roleId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
