package com.ruoyi.merchant.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.factory.RemoteMerchantUserFallbackFactory;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.api.model.MerchantLoginRecordCommand;
import com.ruoyi.merchant.api.model.MerchantOwnerProvisionCommand;

/** 商户身份内部服务契约。 */
@FeignClient(contextId = "remoteMerchantUserService", value = MerchantServiceConstants.SERVICE_NAME,
        fallbackFactory = RemoteMerchantUserFallbackFactory.class)
public interface RemoteMerchantUserService
{
    /**
     * 按用户名查询商户登录身份。
     *
     * @param username 商户用户名
     * @param source 内部服务调用标识
     * @param clientType 请求终端类型，必须为商户端
     * @return 商户登录身份
     */
    @GetMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/users/by-username/{username}")
    R<MerchantLoginIdentity> getByUsername(@PathVariable("username") String username,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /**
     * 按手机号查询商户登录身份，用于商户首次验证码登录。
     *
     * @param phone 商户注册手机号
     * @param source 内部服务调用标识
     * @param clientType 请求终端类型，必须为商户端
     * @return 商户登录身份
     */
    @GetMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/users/by-phone/{phone}")
    R<MerchantLoginIdentity> getByPhone(@PathVariable("phone") String phone,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /**
     * 更新商户用户最近一次登录信息。
     *
     * @param command 登录记录命令
     * @param source 内部服务调用标识
     * @param clientType 请求终端类型，必须为商户端
     * @return 是否更新成功
     */
    @PutMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/users/login-record")
    R<Boolean> recordLogin(@RequestBody MerchantLoginRecordCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /**
     * 幂等开通商户所有者账号、管理员角色和菜单权限。
     *
     * @param command 平台审核服务生成的可信开户命令
     * @param source 内部服务调用标识
     * @param clientType 请求终端类型，必须为商户端
     * @return 开户是否完整成功
     */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/users/owner")
    R<Boolean> provisionOwner(@RequestBody MerchantOwnerProvisionCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);
}
