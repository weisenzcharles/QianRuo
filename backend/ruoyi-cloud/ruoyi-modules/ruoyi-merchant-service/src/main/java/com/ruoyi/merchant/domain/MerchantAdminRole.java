package com.ruoyi.merchant.domain;

/** 审核通过时创建的商户管理员角色。 */
public class MerchantAdminRole
{
    private Long roleId;
    private Long tenantId;
    private Long merchantId;
    private Long operator;

    /** @return 数据库生成的商户角色唯一编号 */
    public Long getRoleId() { return roleId; }

    /** @param roleId 数据库生成的商户角色唯一编号 */
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    /** @return 商户管理员角色所属租户唯一编号 */
    public Long getTenantId() { return tenantId; }

    /** @param tenantId 商户管理员角色所属租户唯一编号 */
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    /** @return 商户管理员角色所属商户唯一编号 */
    public Long getMerchantId() { return merchantId; }

    /** @param merchantId 商户管理员角色所属商户唯一编号 */
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    /** @return 创建角色的平台用户唯一编号 */
    public Long getOperator() { return operator; }

    /** @param operator 创建角色的平台用户唯一编号 */
    public void setOperator(Long operator) { this.operator = operator; }
}
