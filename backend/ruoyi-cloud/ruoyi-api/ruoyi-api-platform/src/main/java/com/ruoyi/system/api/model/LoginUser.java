package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import com.ruoyi.system.api.domain.SysUser;

/**
 * 用户信息
 *
 * @author ruoyi
 */
public class LoginUser implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户名id
     */
    private Long userid;

    /**
     * 用户名
     */
    private String username;

    /** 登录客户端类型：platform 或 merchant */
    private String clientType;

    /** 当前租户ID，仅商户端登录态存在 */
    private Long tenantId;

    /** 当前商户ID，仅商户端登录态存在 */
    private Long memberId;

    /** 当前商户认证状态，非 verified 时业务权限必须为空 */
    private String merchantAuthStatus;

    /** 当前用户在商户内的身份 */
    private String memberRole;

    /** 仓库范围：all 全部、specified 指定 */
    private String warehouseScopeType;

    /** 指定仓库ID集合 */
    private Set<Long> warehouseIds = new LinkedHashSet<Long>();

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * 用户信息
     */
    private SysUser sysUser;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Long getUserid()
    {
        return userid;
    }

    public void setUserid(Long userid)
    {
        this.userid = userid;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getClientType()
    {
        return clientType;
    }

    public void setClientType(String clientType)
    {
        this.clientType = clientType;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    /** @return 当前商户认证状态。 */
    public String getMerchantAuthStatus()
    {
        return merchantAuthStatus;
    }

    /** @param merchantAuthStatus 设置当前商户认证状态。 */
    public void setMerchantAuthStatus(String merchantAuthStatus)
    {
        this.merchantAuthStatus = merchantAuthStatus;
    }

    public String getMemberRole()
    {
        return memberRole;
    }

    public void setMemberRole(String memberRole)
    {
        this.memberRole = memberRole;
    }

    public String getWarehouseScopeType()
    {
        return warehouseScopeType;
    }

    public void setWarehouseScopeType(String warehouseScopeType)
    {
        this.warehouseScopeType = warehouseScopeType;
    }

    public Set<Long> getWarehouseIds()
    {
        return warehouseIds;
    }

    public void setWarehouseIds(Set<Long> warehouseIds)
    {
        this.warehouseIds = warehouseIds == null ? new LinkedHashSet<Long>() : new LinkedHashSet<Long>(warehouseIds);
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles;
    }

    public SysUser getSysUser()
    {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser)
    {
        this.sysUser = sysUser;
    }
}
