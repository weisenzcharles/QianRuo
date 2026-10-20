package com.ruoyi.merchant.model;

import java.util.Date;

/** 当前商户用户列表展示对象，不暴露密码和归属唯一编号。 */
public class MerchantUserVO
{
    private Long userId;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
    private Integer status;
    private Integer mustChangePassword;
    private Date loginDate;
    private Date createTime;
    private String roleNames;
    private Long[] roleIds;
    /** 商户内身份，owner 为商户所有者，其余为普通员工。 */
    private String memberRole;
    /** 仓库范围：1 全部仓库，2 指定仓库。 */
    private Integer warehouseScopeType;
    /** 指定仓库唯一编号，仅作为提交参数使用，页面通过仓库名称展示。 */
    private Long[] warehouseIds;
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Integer value) { this.mustChangePassword = value; }
    public Date getLoginDate() { return loginDate; }
    public void setLoginDate(Date value) { this.loginDate = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { this.createTime = value; }
    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String value) { this.roleNames = value; }
    public Long[] getRoleIds() { return roleIds; }
    public void setRoleIds(Long[] value) { this.roleIds = value; }
    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String value) { this.memberRole = value; }
    public Integer getWarehouseScopeType() { return warehouseScopeType; }
    public void setWarehouseScopeType(Integer value) { this.warehouseScopeType = value; }
    public Long[] getWarehouseIds() { return warehouseIds; }
    public void setWarehouseIds(Long[] value) { this.warehouseIds = value; }
    public String getPassword() { return password; }
    public void setPassword(String value) { this.password = value; }
}
