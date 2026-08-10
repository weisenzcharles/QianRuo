package com.ruoyi.merchant.controller;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.application.MerchantIdentityService;
import com.ruoyi.merchant.application.MerchantSessionService;

/** 商户当前用户入口。 */
@RestController
@RequestMapping("/merchant/user")
public class MerchantUserController
{
    @Autowired
    private MerchantIdentityService identityService;

    @Autowired
    private MerchantSessionService sessionService;

    @Value("${merchant.account.passwordValidateDays:0}")
    private int passwordValidateDays;

    @GetMapping("/getInfo")
    public AjaxResult getInfo()
    {
        MerchantLoginIdentity identity = identityService.loadById(SecurityUtils.getUserId());
        sessionService.refresh(identity);

        AjaxResult result = AjaxResult.success();
        result.put("user", sessionService.toProfile(identity));
        result.put("roles", identity.getRoles());
        result.put("permissions", identity.getPermissions());
        result.put("merchantAuthStatus", identity.getMerchantAuthStatus());
        result.put("featureEnabled", "verified".equals(identity.getMerchantAuthStatus()));
        result.put("mustChangePassword", Boolean.TRUE.equals(identity.getMustChangePassword()));
        result.put("isPasswordExpired", passwordExpired(identity.getPasswordUpdatedAt()));
        return result;
    }

    private boolean passwordExpired(Long passwordUpdatedAt)
    {
        if (passwordValidateDays <= 0 || passwordUpdatedAt == null)
        {
            return false;
        }
        return DateUtils.differentDaysByMillisecond(DateUtils.getNowDate(), new Date(passwordUpdatedAt))
                > passwordValidateDays;
    }
}
