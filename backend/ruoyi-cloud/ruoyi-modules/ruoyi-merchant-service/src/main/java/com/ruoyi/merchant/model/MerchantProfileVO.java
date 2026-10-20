package com.ruoyi.merchant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/** 商户个人资料视图。 */
public class MerchantProfileVO
{
    private Long userId;
    private String userName;
    private String nickName;
    private String phonenumber;
    private String email;
    private String avatar;
    private Integer mustChangePassword;
    private Date createTime;
    private List<String> roleNames = new ArrayList<String>();
    private String memberRole;
    private Integer warehouseScopeType;
    private Integer status;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Integer mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public List<String> getRoleNames() { return roleNames; }
    public void setRoleNames(List<String> roleNames) { this.roleNames = roleNames == null ? new ArrayList<String>() : roleNames; }
    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }
    public Integer getWarehouseScopeType() { return warehouseScopeType; }
    public void setWarehouseScopeType(Integer warehouseScopeType) { this.warehouseScopeType = warehouseScopeType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
