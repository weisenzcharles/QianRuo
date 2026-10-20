package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.ruoyi.common.alipay.AlipayClientConfig;
import com.ruoyi.common.alipay.AlipayPaymentClient;
import com.ruoyi.common.alipay.AlipayPaymentException;
import com.ruoyi.common.alipay.AlipayTradeResult;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.RemoteRentalBusinessService;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentCompletionCommand;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentTarget;
import com.ruoyi.merchant.config.GlobalConfigService;
import com.ruoyi.merchant.mapper.MerchantPaymentMapper;
import com.ruoyi.merchant.model.MerchantPaymentBizType;
import com.ruoyi.merchant.model.MerchantPaymentRecord;
import com.ruoyi.merchant.model.MerchantPaymentSettlementResult;
import com.ruoyi.merchant.model.MerchantPaymentVO;

/** 商户端支付宝充值和订单支付应用服务。 */
@Service
public class MerchantAlipayPaymentService
{
    private static final DateTimeFormatter PAYMENT_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final BigDecimal MAX_RECHARGE_AMOUNT = new BigDecimal("1000000.00");

    private final MerchantPaymentMapper mapper;
    private final MerchantPaymentOrderService paymentOrderService;
    private final MerchantPaymentSettlementService settlementService;
    private final RemoteRentalBusinessService rentalBusinessService;
    private final GlobalConfigService globalConfigService;

    public MerchantAlipayPaymentService(MerchantPaymentMapper mapper, MerchantPaymentOrderService paymentOrderService,
            MerchantPaymentSettlementService settlementService, RemoteRentalBusinessService rentalBusinessService,
            GlobalConfigService globalConfigService)
    {
        this.mapper = mapper;
        this.paymentOrderService = paymentOrderService;
        this.settlementService = settlementService;
        this.rentalBusinessService = rentalBusinessService;
        this.globalConfigService = globalConfigService;
    }

    /** 创建商户账户充值支付宝支付单。 */
    public MerchantPaymentVO createRecharge(BigDecimal amount)
    {
        MerchantContextUtils.requireMerchantApproved();
        BigDecimal normalizedAmount = normalizeAmount(amount);
        AlipayClientConfig config = loadConfig(true);
        String paymentNo = createPaymentNo("RCH");
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(parseTimeoutMinutes(config.getTimeoutExpress()));
        String subject = "商户账户充值";
        String form = new AlipayPaymentClient(config).createPageForm(subject, paymentNo, normalizedAmount,
                config.getNotifyUrl(), config.getReturnUrl());
        mapper.insertPaymentOrder(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), paymentNo,
                MerchantPaymentBizType.RECHARGE.getCode(), null, paymentNo, subject, normalizedAmount, expireTime,
                SecurityUtils.getUserId(), RentalCode.PaymentStatus.UNPAID.getCode());
        MerchantPaymentRecord payment = mapper.selectByPaymentNo(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), paymentNo);
        return toVO(payment, form);
    }

    /** 创建订单支付宝支付单，订单金额和归属由租赁业务服务提供。 */
    public MerchantPaymentVO createOrderPayment(Long orderId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null)
        {
            throw new ServiceException("订单唯一编号不能为空");
        }
        R<MerchantOrderPaymentTarget> response = rentalBusinessService.getOrderPaymentTarget(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), orderId, SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response) || response.getData() == null)
        {
            throw new ServiceException(response == null ? "租赁业务服务暂不可用" : response.getMsg());
        }
        MerchantOrderPaymentTarget target = response.getData();
        if (!RentalCode.PaymentStatus.UNPAID.getCode().equals(target.getPayStatus()))
        {
            throw new ServiceException("订单当前无需支付");
        }
        if (RentalCode.OrderStatus.CANCELLED.getCode().equals(target.getOrderStatus())
                || RentalCode.OrderStatus.COMPLETED.getCode().equals(target.getOrderStatus()))
        {
            throw new ServiceException("当前订单状态不可支付");
        }
        BigDecimal amount = normalizeAmount(target.getAmount());
        AlipayClientConfig config = loadConfig(true);
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(parseTimeoutMinutes(config.getTimeoutExpress()));
        String subject = target.getSubject() == null || target.getSubject().isBlank()
                ? "租赁订单 " + target.getOrderNo() + " 支付" : target.getSubject();
        MerchantPaymentRecord payment = paymentOrderService.createOrReuse(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), target.getOrderId(), target.getOrderNo(), subject, amount,
                expireTime, SecurityUtils.getUserId());
        String form = new AlipayPaymentClient(config).createPageForm(subject, payment.getPaymentNo(),
                payment.getAmount(), config.getNotifyUrl(), config.getReturnUrl());
        return toVO(payment, form);
    }

    /** 查询当前商户支付单状态，用于支付窗口关闭后的前端轮询。 */
    public MerchantPaymentVO getPayment(String paymentNo)
    {
        MerchantPaymentRecord payment = mapper.selectByPaymentNo(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), paymentNo);
        return payment == null ? null : toVO(payment, null);
    }

    /** 处理支付宝异步通知；验签成功但非终态通知直接确认接收。 */
    public void handleNotify(Map<String, String> params)
    {
        AlipayClientConfig config = loadConfig(false);
        AlipayPaymentClient client = new AlipayPaymentClient(config);
        if (!client.verifyNotify(params))
        {
            throw new AlipayPaymentException("支付宝异步通知签名无效");
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus))
        {
            return;
        }
        String paymentNo = params.get("out_trade_no");
        String alipayTradeNo = params.get("trade_no");
        BigDecimal totalAmount = decimal(params.get("total_amount"));
        if (paymentNo == null || alipayTradeNo == null || totalAmount == null)
        {
            throw new ServiceException("支付宝通知缺少支付结果");
        }
        MerchantPaymentSettlementResult result = settlementService.settle(paymentNo, alipayTradeNo,
                params.get("notify_id"), totalAmount);
        MerchantPaymentRecord payment = result.getPayment();
        MerchantPaymentBizType bizType = MerchantPaymentBizType.fromCode(payment.getBizType());
        if (bizType == MerchantPaymentBizType.ORDER && payment.getBizId() != null)
        {
            MerchantOrderPaymentCompletionCommand command = new MerchantOrderPaymentCompletionCommand();
            command.setTenantId(payment.getTenantId());
            command.setMemberId(payment.getMemberId());
            command.setOrderId(payment.getBizId());
            command.setPaymentNo(payment.getPaymentNo());
            command.setAlipayTradeNo(alipayTradeNo);
            R<Boolean> response = rentalBusinessService.completeOrderPayment(command, SecurityConstants.INNER);
            if (response == null || !R.isSuccess(response) || !Boolean.TRUE.equals(response.getData()))
            {
                throw new ServiceException(response == null ? "订单付款状态同步失败" : response.getMsg());
            }
        }
    }

    private AlipayClientConfig loadConfig(boolean requireEnabled)
    {
        if (requireEnabled && !globalConfigService.isEnabled(GlobalConfigConstants.ALIPAY_ENABLED))
        {
            throw new ServiceException("支付宝支付尚未启用，请联系平台管理员配置");
        }
        AlipayClientConfig config = new AlipayClientConfig();
        config.setServerUrl(globalConfigService.get(GlobalConfigConstants.ALIPAY_SERVER_URL,
                "https://openapi.alipay.com/gateway.do"));
        config.setAppId(globalConfigService.get(GlobalConfigConstants.ALIPAY_APP_ID));
        config.setPrivateKey(globalConfigService.get(GlobalConfigConstants.ALIPAY_PRIVATE_KEY));
        config.setAlipayPublicKey(globalConfigService.get(GlobalConfigConstants.ALIPAY_PUBLIC_KEY));
        config.setSignType(globalConfigService.get(GlobalConfigConstants.ALIPAY_SIGN_TYPE, "RSA2"));
        config.setCharset(globalConfigService.get(GlobalConfigConstants.ALIPAY_CHARSET, "utf-8"));
        config.setFormat(globalConfigService.get(GlobalConfigConstants.ALIPAY_FORMAT, "json"));
        config.setTimeoutExpress(globalConfigService.get(GlobalConfigConstants.ALIPAY_TIMEOUT_EXPRESS, "15m"));
        config.setNotifyUrl(globalConfigService.get(GlobalConfigConstants.ALIPAY_NOTIFY_URL));
        config.setReturnUrl(globalConfigService.get(GlobalConfigConstants.ALIPAY_RETURN_URL));
        if (config.getNotifyUrl() == null || config.getNotifyUrl().isBlank())
        {
            throw new ServiceException("支付宝异步通知地址未配置");
        }
        try
        {
            config.validate();
        }
        catch (AlipayPaymentException exception)
        {
            throw new ServiceException(exception.getMessage());
        }
        return config;
    }

    private BigDecimal normalizeAmount(BigDecimal amount)
    {
        if (amount == null || amount.scale() > 2 || amount.signum() <= 0 || amount.compareTo(MAX_RECHARGE_AMOUNT) > 0)
        {
            throw new ServiceException("支付金额必须大于 0 且不超过 1000000 元，最多保留两位小数");
        }
        return amount.setScale(2);
    }

    private BigDecimal decimal(String value)
    {
        try
        {
            return value == null || value.isBlank() ? null : new BigDecimal(value).setScale(2);
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    private String createPaymentNo(String prefix)
    {
        return prefix + LocalDateTime.now().format(PAYMENT_NO_TIME)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private long parseTimeoutMinutes(String timeoutExpress)
    {
        if (timeoutExpress == null || timeoutExpress.isBlank()) return 15L;
        String value = timeoutExpress.trim().toLowerCase();
        try
        {
            if (value.endsWith("h")) return Math.max(1L, Long.parseLong(value.substring(0, value.length() - 1)) * 60L);
            if (value.endsWith("d")) return Math.max(1L, Long.parseLong(value.substring(0, value.length() - 1)) * 1440L);
            if (value.endsWith("m")) return Math.max(1L, Long.parseLong(value.substring(0, value.length() - 1)));
            return Math.max(1L, Long.parseLong(value));
        }
        catch (NumberFormatException exception)
        {
            return 15L;
        }
    }

    private MerchantPaymentVO toVO(MerchantPaymentRecord payment, String paymentForm)
    {
        if (payment == null) throw new ServiceException("支付单创建失败");
        MerchantPaymentVO result = new MerchantPaymentVO();
        MerchantPaymentBizType bizType = MerchantPaymentBizType.fromCode(payment.getBizType());
        result.setPaymentNo(payment.getPaymentNo());
        result.setBizType(payment.getBizType());
        result.setBizTypeName(bizType == null ? "其他支付" : bizType.getLabel());
        result.setBizNo(payment.getBizNo());
        result.setSubject(payment.getSubject());
        result.setAmount(payment.getAmount());
        result.setPaymentStatus(payment.getPaymentStatus());
        result.setPaymentStatusName(paymentStatusName(payment.getPaymentStatus()));
        result.setPaymentForm(paymentForm);
        result.setPaidTime(payment.getPaidTime() == null ? null : payment.getPaidTime().toString());
        result.setExpireTime(payment.getExpireTime() == null ? null : payment.getExpireTime().toString());
        return result;
    }

    private String paymentStatusName(Integer status)
    {
        if (status == null) return "未知状态";
        for (RentalCode.PaymentStatus item : RentalCode.PaymentStatus.values())
        {
            if (item.getCode().equals(status)) return item.getLabel();
        }
        return "未知状态";
    }
}
