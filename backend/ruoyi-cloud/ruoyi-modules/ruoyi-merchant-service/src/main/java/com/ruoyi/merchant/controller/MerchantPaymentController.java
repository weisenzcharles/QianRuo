package com.ruoyi.merchant.controller;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantAlipayPaymentService;
import com.ruoyi.merchant.model.MerchantPaymentVO;

/** 商户充值、订单支付和支付宝服务器通知接口。 */
@RestController
@RequestMapping("/merchant")
public class MerchantPaymentController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(MerchantPaymentController.class);

    private final MerchantAlipayPaymentService paymentService;

    public MerchantPaymentController(MerchantAlipayPaymentService paymentService)
    {
        this.paymentService = paymentService;
    }

    /** 创建充值支付单并返回支付宝电脑网站支付表单。 */
    @RequiresPermissions("merchant:finance:account")
    @PostMapping("/payments/recharge")
    public AjaxResult recharge(@RequestBody Map<String, BigDecimal> body)
    {
        BigDecimal amount = body == null ? null : body.get("amount");
        return success(paymentService.createRecharge(amount));
    }

    /** 创建订单支付单并返回支付宝电脑网站支付表单。 */
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/payments/orders/{orderId}")
    public AjaxResult orderPayment(@PathVariable Long orderId)
    {
        return success(paymentService.createOrderPayment(orderId));
    }

    /** 查询当前商户支付单状态。 */
    @RequiresPermissions("merchant:finance:account")
    @GetMapping("/payments/{paymentNo}")
    public AjaxResult payment(@PathVariable String paymentNo)
    {
        MerchantPaymentVO result = paymentService.getPayment(paymentNo);
        return result == null ? error("支付单不存在或无权查看") : success(result);
    }

    /**
     * 接收支付宝异步通知。支付宝要求成功时返回精确的 {@code success}，失败返回 {@code fail} 触发重试。
     */
    @PostMapping("/public/alipay/notify")
    public String alipayNotify(@RequestParam Map<String, String> params)
    {
        try
        {
            paymentService.handleNotify(params);
            return "success";
        }
        catch (RuntimeException exception)
        {
            log.warn("支付宝异步通知处理失败，将等待支付宝重试: {}", exception.getMessage());
            return "fail";
        }
    }
}
