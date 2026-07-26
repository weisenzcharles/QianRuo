package com.ruoyi.merchant.controller.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.merchant.api.MerchantServiceConstants;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.api.model.MerchantLoginRecordCommand;
import com.ruoyi.merchant.api.model.MerchantOwnerProvisionCommand;
import com.ruoyi.merchant.application.MerchantIdentityService;

/** 认证服务使用的商户身份内部入口。 */
@RestController
@RequestMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/users")
public class InternalMerchantIdentityController
{
    @Autowired
    private MerchantIdentityService identityService;

    /**
     * 供认证服务按用户名加载商户登录身份，仅允许携带内部调用标识的请求访问。
     *
     * @param username 商户用户名
     * @return 商户登录身份
     */
    @InnerAuth
    @GetMapping("/by-username/{username}")
    public R<MerchantLoginIdentity> byUsername(@PathVariable String username)
    {
        return R.ok(identityService.loadByUsername(username));
    }

    /**
     * 供认证服务按手机号加载商户登录身份，用于商户首次验证码登录。
     *
     * @param phone 商户入驻手机号
     * @return 商户登录身份
     */
    @InnerAuth
    @GetMapping("/by-phone/{phone}")
    public R<MerchantLoginIdentity> byPhone(@PathVariable String phone)
    {
        return R.ok(identityService.loadByPhone(phone));
    }

    /**
     * 供认证服务记录商户用户登录信息。
     *
     * @param command 商户用户主键、登录地址和登录时间
     * @return 登录记录是否更新成功
     */
    @InnerAuth
    @PutMapping("/login-record")
    public R<Boolean> recordLogin(@RequestBody MerchantLoginRecordCommand command)
    {
        return R.ok(identityService.recordLogin(command.getUserId(), command.getLoginIp(), command.getLoginAt()));
    }

    /**
     * 供平台审核服务幂等开通商户所有者账号及管理员权限。
     *
     * @param command 平台服务生成的可信开户命令
     * @return 开户是否完整成功
     */
    @InnerAuth
    @PostMapping("/owner")
    public R<Boolean> provisionOwner(@RequestBody MerchantOwnerProvisionCommand command)
    {
        return R.ok(identityService.provisionOwner(command));
    }
}
