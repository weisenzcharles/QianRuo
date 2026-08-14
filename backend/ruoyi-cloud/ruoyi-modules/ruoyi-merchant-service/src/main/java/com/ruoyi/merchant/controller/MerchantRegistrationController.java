package com.ruoyi.merchant.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.application.MerchantRegistrationService;
import com.ruoyi.merchant.model.MerchantRegistrationAccessCommand;
import com.ruoyi.merchant.model.MerchantRegistrationCommand;
import com.ruoyi.merchant.model.MerchantRegistrationCodeCommand;
import com.ruoyi.merchant.model.MerchantRegistrationVO;

/** 商户端公开入驻入口。 */
@RestController
@RequestMapping("/merchant/public/registration")
public class MerchantRegistrationController
{
    @Autowired
    private MerchantRegistrationService registrationService;

    /**
     * 获取商户入驻验证码。当前未接入短信供应商，仅校验手机号并允许后续使用任意四位数字。
     *
     * @param command 申请手机号
     * @return 验证码请求是否受理
     */
    @PostMapping("/code")
    public R<Boolean> requestCode(@Valid @RequestBody MerchantRegistrationCodeCommand command)
    {
        return R.ok(registrationService.requestCode(command));
    }

    /**
     * 提交新的商户入驻申请，归属字段和审核状态均由平台服务生成。
     *
     * @param command 商户入驻资料
     * @return 待审核申请资料
     */
    @PostMapping
    public R<MerchantRegistrationVO> submit(@Valid @RequestBody MerchantRegistrationCommand command)
    {
        return R.ok(registrationService.submit(command));
    }

    /**
     * 使用手机号和验证码查询已提交的商户申请。
     *
     * @param command 手机号验证码凭证
     * @return 当前申请状态和资料
     */
    @PostMapping("/lookup")
    public R<MerchantRegistrationVO> lookup(@Valid @RequestBody MerchantRegistrationAccessCommand command)
    {
        return R.ok(registrationService.lookup(command));
    }

    /**
     * 修改并重新提交审核未通过的商户申请。
     *
     * @param command 修改后的完整申请资料
     * @return 重新进入待审核状态的申请资料
     */
    @PutMapping
    public R<MerchantRegistrationVO> resubmit(@Valid @RequestBody MerchantRegistrationCommand command)
    {
        return R.ok(registrationService.resubmit(command));
    }

    /**
     * 确认付费注册支付完成并进入注册完成状态。
     *
     * @param paymentNo 注册支付单号
     * @return 注册完成后的申请资料
     */
    @PostMapping("/payment/{paymentNo}/confirm")
    public R<MerchantRegistrationVO> confirmPayment(@PathVariable String paymentNo)
    {
        return R.ok(registrationService.confirmPayment(paymentNo));
    }
}
