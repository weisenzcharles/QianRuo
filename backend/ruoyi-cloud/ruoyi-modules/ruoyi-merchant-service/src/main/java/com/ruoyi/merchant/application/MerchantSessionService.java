package com.ruoyi.merchant.application;

import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.model.MerchantProfileVO;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 将独立商户身份写入框架统一会话。此适配仅位于认证边界，不进入商户 API 契约。
 */
@Service
public class MerchantSessionService
{
    @Autowired
    private TokenService tokenService;

    public void refresh(MerchantLoginIdentity identity)
    {
        LoginUser session = SecurityUtils.getLoginUser();
        apply(session, identity);
        tokenService.setLoginUser(session);
    }

    public LoginUser apply(LoginUser session, MerchantLoginIdentity identity)
    {
        if (session == null || !Constants.CLIENT_TYPE_MERCHANT.equals(session.getClientType()))
        {
            throw new ServiceException("当前登录身份不是商户用户");
        }
        SysUser user = new SysUser();
        user.setUserId(identity.getUserId());
        user.setUserName(identity.getUsername());
        user.setNickName(identity.getNickname());
        user.setPhonenumber(identity.getPhone());
        user.setEmail(identity.getEmail());
        user.setAvatar(identity.getAvatar());
        user.setPassword(identity.getPassword());
        user.setStatus(identity.getStatus());
        user.setIsDelete(identity.getIsDelete());
        user.setPwdUpdateDate(identity.getPasswordUpdatedAt() == null
                ? null : new Date(identity.getPasswordUpdatedAt()));
        user.setMustChangePassword(identity.getMustChangePassword());

        session.setUserid(identity.getUserId());
        session.setUsername(identity.getUsername());
        session.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        session.setTenantId(identity.getTenantId());
        session.setMemberId(identity.getMerchantId());
        session.setMerchantAuthStatus(identity.getMerchantAuthStatus());
        session.setMemberRole(identity.getMemberRole());
        session.setWarehouseScopeType(identity.getWarehouseScopeType());
        session.setWarehouseIds(identity.getWarehouseIds());
        session.setRoles(identity.getRoles());
        session.setPermissions(identity.getPermissions());
        session.setSysUser(user);
        return session;
    }

    public MerchantProfileVO toProfile(MerchantLoginIdentity identity)
    {
        MerchantProfileVO profile = new MerchantProfileVO();
        profile.setUserId(identity.getUserId());
        profile.setUserName(identity.getUsername());
        profile.setNickName(identity.getNickname());
        profile.setPhonenumber(identity.getPhone());
        profile.setEmail(identity.getEmail());
        profile.setAvatar(identity.getAvatar());
        profile.setMustChangePassword(identity.getMustChangePassword());
        profile.setCreateTime(identity.getCreatedAt() == null ? null : new Date(identity.getCreatedAt()));
        return profile;
    }
}
