package com.ruoyi.system.domain.rental;

import java.util.Date;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 商户端用户
 *
 * @author qianruo
 */
public class RentalMerchantUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商户用户ID */
    private Long merchantUserId;

    /** 租户ID */
    private Long tenantId;

    /** 商户ID */
    private Long memberId;

    /** 登录账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像 */
    private String avatar;

    /** 登录密码 */
    private String password;

    /** 账号状态 */
    private String status;

    /** 删除标志 */
    private String isdeleted;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    private Date loginDate;

    /** 密码最后更新时间 */
    private Date pwdUpdateDate;

    /** 是否必须修改初始密码 */
    private Boolean mustChangePassword;

    /** 商户主体名称 */
    private String legalSubjectName;

    /** 商户昵称 */
    private String memberNickname;

    /** 角色组 */
    private Long[] roleIds;

    /** 菜单组 */
    private Long[] menuIds;

    /** 商户内身份 */
    private String memberRole;

    /** 仓库范围：all 全部、specified 指定 */
    private String warehouseScopeType;

    /** 指定仓库ID集合 */
    private Long[] warehouseIds;

    public Long getMerchantUserId()
    {
        return merchantUserId;
    }

    public void setMerchantUserId(Long merchantUserId)
    {
        this.merchantUserId = merchantUserId;
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

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getIsdeleted()
    {
        return isdeleted;
    }

    public void setIsdeleted(String isdeleted)
    {
        this.isdeleted = isdeleted;
    }

    public String getLoginIp()
    {
        return loginIp;
    }

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public Date getLoginDate()
    {
        return loginDate;
    }

    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }

    public Date getPwdUpdateDate()
    {
        return pwdUpdateDate;
    }

    public void setPwdUpdateDate(Date pwdUpdateDate)
    {
        this.pwdUpdateDate = pwdUpdateDate;
    }

    public Boolean getMustChangePassword()
    {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword)
    {
        this.mustChangePassword = mustChangePassword;
    }

    public String getLegalSubjectName()
    {
        return legalSubjectName;
    }

    public void setLegalSubjectName(String legalSubjectName)
    {
        this.legalSubjectName = legalSubjectName;
    }

    public String getMemberNickname()
    {
        return memberNickname;
    }

    public void setMemberNickname(String memberNickname)
    {
        this.memberNickname = memberNickname;
    }

    public Long[] getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds)
    {
        this.roleIds = roleIds;
    }

    public Long[] getMenuIds()
    {
        return menuIds;
    }

    public void setMenuIds(Long[] menuIds)
    {
        this.menuIds = menuIds;
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

    public Long[] getWarehouseIds()
    {
        return warehouseIds;
    }

    public void setWarehouseIds(Long[] warehouseIds)
    {
        this.warehouseIds = warehouseIds;
    }
}
