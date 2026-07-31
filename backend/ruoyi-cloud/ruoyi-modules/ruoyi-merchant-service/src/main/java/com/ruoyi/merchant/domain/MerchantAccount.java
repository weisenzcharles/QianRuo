package com.ruoyi.merchant.domain;

import java.util.Date;

/** 商户账号持久化对象。 */
public class MerchantAccount
{
    private Long userId;
    private Long tenantId;
    private Long merchantId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String password;
    private String status;
    private String isdeleted;
    private String loginIp;
    private Date loginDate;
    private Date passwordUpdateDate;
    private Boolean mustChangePassword;
    private Date createTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIsdeleted() { return isdeleted; }
    public void setIsdeleted(String isdeleted) { this.isdeleted = isdeleted; }
    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }
    public Date getLoginDate() { return loginDate; }
    public void setLoginDate(Date loginDate) { this.loginDate = loginDate; }
    public Date getPasswordUpdateDate() { return passwordUpdateDate; }
    public void setPasswordUpdateDate(Date passwordUpdateDate) { this.passwordUpdateDate = passwordUpdateDate; }
    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
