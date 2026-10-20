package com.ruoyi.system.controller.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteMerchantRegistrationService;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationCommand;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationVO;
import com.ruoyi.system.service.rental.PlatformMerchantRegistrationService;

/** 商户服务调用的平台入驻申请内部入口。 */
@RestController
@RequestMapping(RemoteMerchantRegistrationService.INTERNAL_PREFIX)
public class InternalMerchantRegistrationController
{
    @Autowired
    private PlatformMerchantRegistrationService registrationService;

    /**
     * 接收商户服务提交的新入驻申请，仅允许内部服务调用。
     *
     * @param command 商户申请资料
     * @return 平台生成的待审核申请结果
     */
    @InnerAuth
    @PostMapping
    public R<MerchantRegistrationApplicationVO> submit(@RequestBody MerchantRegistrationApplicationCommand command)
    {
        return R.ok(registrationService.submit(command));
    }

    /**
     * 按注册手机号查询平台持有的商户申请，仅允许内部服务调用。
     *
     * @param phone 商户注册手机号
     * @return 当前申请状态和资料
     */
    @InnerAuth
    @GetMapping("/phone/{phone}")
    public R<MerchantRegistrationApplicationVO> getByPhone(@PathVariable String phone)
    {
        return R.ok(registrationService.getByPhone(phone));
    }

    /**
     * 接收商户服务重新提交的驳回申请，仅允许内部服务调用。
     *
     * @param command 修改后的完整申请资料
     * @return 重新进入待审核状态的申请结果
     */
    @InnerAuth
    @PutMapping
    public R<MerchantRegistrationApplicationVO> resubmit(@RequestBody MerchantRegistrationApplicationCommand command)
    {
        return R.ok(registrationService.resubmit(command));
    }

    /**
     * 完成付费注册支付确认。
     *
     * @param paymentNo 注册支付单号
     * @return 注册完成后的申请资料
     */
    @InnerAuth
    @PostMapping("/payment/{paymentNo}/confirm")
    public R<MerchantRegistrationApplicationVO> confirmPayment(@PathVariable String paymentNo)
    {
        return R.ok(registrationService.confirmPayment(paymentNo));
    }
}
