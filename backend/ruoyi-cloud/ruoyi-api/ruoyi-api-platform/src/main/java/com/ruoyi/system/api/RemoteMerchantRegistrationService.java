package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteMerchantRegistrationFallbackFactory;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationCommand;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationVO;

/** 商户服务访问的平台入驻申请内部契约。 */
@FeignClient(contextId = "remoteMerchantRegistrationService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteMerchantRegistrationFallbackFactory.class)
public interface RemoteMerchantRegistrationService
{
    String INTERNAL_PREFIX = "/internal/platform/merchant-registration";

    /**
     * 向平台服务提交新的商户入驻申请。
     *
     * @param command 商户申请资料，不包含租户和商户主键
     * @param source 内部服务调用标识
     * @return 平台生成的待审核申请资料
     */
    @PostMapping(INTERNAL_PREFIX)
    R<MerchantRegistrationApplicationVO> submit(@RequestBody MerchantRegistrationApplicationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据注册手机号查询平台持有的商户申请。
     *
     * @param phone 商户注册手机号
     * @param source 内部服务调用标识
     * @return 当前申请状态和可编辑资料
     */
    @GetMapping(INTERNAL_PREFIX + "/phone/{phone}")
    R<MerchantRegistrationApplicationVO> getByPhone(@PathVariable("phone") String phone,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 将审核未通过的商户申请修改后重新提交。
     *
     * @param command 修改后的完整申请资料
     * @param source 内部服务调用标识
     * @return 重新进入待审核状态的申请资料
     */
    @PutMapping(INTERNAL_PREFIX)
    R<MerchantRegistrationApplicationVO> resubmit(@RequestBody MerchantRegistrationApplicationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 确认注册支付单已完成支付。
     *
     * @param paymentNo 注册支付单号
     * @param source 内部调用标识
     * @return 完成注册后的申请资料
     */
    @PostMapping(INTERNAL_PREFIX + "/payment/{paymentNo}/confirm")
    R<MerchantRegistrationApplicationVO> confirmPayment(@PathVariable("paymentNo") String paymentNo,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
