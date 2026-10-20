package com.ruoyi.merchant.controller.internal;

import java.util.List;
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
import com.ruoyi.merchant.api.model.MerchantUserSummaryQuery;
import com.ruoyi.merchant.api.model.MerchantUserSummary;
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
     * 供业务服务批量查询操作人展示信息，不返回密码等认证字段。
     *
     * @param query 带租户、商户和用户唯一编号范围的查询命令
     * @return 用户展示摘要
     */
    @InnerAuth
    @PostMapping("/summaries")
    public R<List<MerchantUserSummary>> summaries(@RequestBody MerchantUserSummaryQuery query)
    {
        return R.ok(identityService.listSummaries(query));
    }

    /**
     * 供业务服务查询当前商户仍可被指派的用户，不返回认证敏感字段。
     *
     * @param query 带租户、商户和可选用户唯一编号范围的查询命令
     * @return 未删除且启用的用户展示摘要
     */
    @InnerAuth
    @PostMapping("/active-summaries")
    public R<List<MerchantUserSummary>> activeSummaries(@RequestBody MerchantUserSummaryQuery query)
    {
        return R.ok(identityService.listActiveSummaries(query));
    }

    /**
     * 供认证服务记录商户用户登录信息。
     *
     * @param command 商户用户唯一编号、登录地址和登录时间
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
