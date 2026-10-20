package com.ruoyi.merchant.api.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 商户认证身份。该契约不依赖平台用户模型。
 */
public class MerchantLoginIdentity implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String password;
    private Integer status;
    private Integer isDelete;
    private Long passwordUpdatedAt;
    private Integer mustChangePassword;
    private Long createdAt;
    private Long tenantId;
    private Long merchantId;
    /** 平台商户认证状态：0未认证 1待支付 2待审核 3已认证 4已驳回。 */
    private Integer merchantAuthStatus;
    private String memberRole;
    private Integer warehouseScopeType;
    private Set<Long> warehouseIds = new LinkedHashSet<Long>();
    private Set<String> roles = new LinkedHashSet<String>();
    private Set<String> permissions = new LinkedHashSet<String>();

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public Long getPasswordUpdatedAt() { return passwordUpdatedAt; }
    public void setPasswordUpdatedAt(Long passwordUpdatedAt) { this.passwordUpdatedAt = passwordUpdatedAt; }
    public Integer getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Integer mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    /** @return 当前商户认证状态。 */
    public Integer getMerchantAuthStatus() { return merchantAuthStatus; }
    /** @param merchantAuthStatus 设置当前商户认证状态。 */
    public void setMerchantAuthStatus(Integer merchantAuthStatus) { this.merchantAuthStatus = merchantAuthStatus; }
    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }
    public Integer getWarehouseScopeType() { return warehouseScopeType; }
    public void setWarehouseScopeType(Integer warehouseScopeType) { this.warehouseScopeType = warehouseScopeType; }
    public Set<Long> getWarehouseIds() { return warehouseIds; }
    public void setWarehouseIds(Set<Long> warehouseIds) { this.warehouseIds = copy(warehouseIds); }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = copy(roles); }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = copy(permissions); }

    private static <T> Set<T> copy(Set<T> source)
    {
        return source == null ? new LinkedHashSet<T>() : new LinkedHashSet<T>(source);
    }
}
