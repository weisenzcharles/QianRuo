package com.ruoyi.merchant.model;

import java.util.Date;

/** 当前商户用户列表展示对象，不暴露密码和归属主键。 */
public class MerchantUserVO
{
    private Long userId;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
    private String status;
    private Boolean mustChangePassword;
    private Date loginDate;
    private Date createTime;
    private String roleNames;
    private Long[] roleIds;
    private String password;
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean value) { this.mustChangePassword = value; }
    public Date getLoginDate() { return loginDate; }
    public void setLoginDate(Date value) { this.loginDate = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { this.createTime = value; }
    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String value) { this.roleNames = value; }
    public Long[] getRoleIds() { return roleIds; }
    public void setRoleIds(Long[] value) { this.roleIds = value; }
    public String getPassword() { return password; }
    public void setPassword(String value) { this.password = value; }
}
