package com.ruoyi.merchant.api.model;

import java.io.Serializable;

/** 平台审核通过后开通商户管理员的内部命令。 */
public class MerchantOwnerProvisionCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private Long merchantId;
    private String phone;
    private String nickname;
    private String email;
    /** 平台审核操作人的 sys_user.user_id；公开注册时允许为空。 */
    private Long operator;

    /** @return 平台确定的租户唯一编号 */
    public Long getTenantId() { return tenantId; }

    /** @param tenantId 平台确定的租户唯一编号 */
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    /** @return 审核通过的商户唯一编号 */
    public Long getMerchantId() { return merchantId; }

    /** @param merchantId 审核通过的商户唯一编号 */
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    /** @return 商户所有者登录手机号 */
    public String getPhone() { return phone; }

    /** @param phone 商户所有者登录手机号 */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return 商户所有者显示名称 */
    public String getNickname() { return nickname; }

    /** @param nickname 商户所有者显示名称 */
    public void setNickname(String nickname) { this.nickname = nickname; }

    /** @return 商户所有者联系邮箱 */
    public String getEmail() { return email; }

    /** @param email 商户所有者联系邮箱 */
    public void setEmail(String email) { this.email = email; }

    /** @return 执行审核的平台用户唯一编号 */
    public Long getOperator() { return operator; }

    /** @param operator 执行审核的平台用户唯一编号 */
    public void setOperator(Long operator) { this.operator = operator; }
}
