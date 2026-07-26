package com.ruoyi.merchant.model;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户角色查询条件。归属字段由服务端补充。 */
public class MerchantRoleQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String roleName;
    private String roleKey;
    private String status;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
