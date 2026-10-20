package com.ruoyi.common.alipay;

import java.math.BigDecimal;
import java.util.Map;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;

/**
 * 支付宝电脑网站支付和异步通知公共客户端。
 *
 * <p>业务服务只负责组装业务订单和处理幂等入账，签名、验签和支付宝协议细节集中在这里。</p>
 */
public class AlipayPaymentClient
{
    private final AlipayClient client;
    private final AlipayClientConfig config;

    public AlipayPaymentClient(AlipayClientConfig config)
    {
        config.validate();
        this.config = config;
        this.client = new DefaultAlipayClient(config.getServerUrl(), config.getAppId(), config.getPrivateKey(),
                config.getFormat(), config.getCharset(), config.getAlipayPublicKey(), config.getSignType());
    }

    /** 创建电脑网站支付表单，表单由调用方写入独立窗口提交给支付宝。 */
    public String createPageForm(String subject, String outTradeNo, BigDecimal totalAmount, String notifyUrl,
            String returnUrl)
    {
        if (subject == null || subject.isBlank() || outTradeNo == null || outTradeNo.isBlank()
                || totalAmount == null || totalAmount.signum() <= 0)
        {
            throw new AlipayPaymentException("支付宝支付订单参数不完整");
        }
        try
        {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(totalAmount.setScale(2).toPlainString());
            model.setSubject(subject);
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            model.setTimeoutExpress(config.getTimeoutExpress());
            request.setBizModel(model);
            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (response == null || !response.isSuccess())
            {
                throw new AlipayPaymentException("支付宝支付下单失败");
            }
            return response.getBody();
        }
        catch (AlipayApiException exception)
        {
            throw new AlipayPaymentException("支付宝支付下单失败", exception);
        }
    }

    /** 查询支付宝交易状态，供通知重试或人工核对时使用。 */
    public AlipayTradeResult query(String outTradeNo)
    {
        if (outTradeNo == null || outTradeNo.isBlank())
        {
            throw new AlipayPaymentException("支付宝商户订单号不能为空");
        }
        try
        {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);
            AlipayTradeQueryResponse response = client.execute(request);
            AlipayTradeResult result = new AlipayTradeResult();
            result.setSuccess(response != null && response.isSuccess());
            if (response != null)
            {
                result.setTradeStatus(response.getTradeStatus());
                result.setTradeNo(response.getTradeNo());
                result.setOutTradeNo(response.getOutTradeNo());
                result.setTotalAmount(toAmount(response.getTotalAmount()));
                result.setResponseBody(response.getBody());
            }
            return result;
        }
        catch (AlipayApiException exception)
        {
            throw new AlipayPaymentException("支付宝交易查询失败", exception);
        }
    }

    /** 校验支付宝异步通知签名。调用方仍需校验订单号、金额和交易状态。 */
    public boolean verifyNotify(Map<String, String> params)
    {
        if (params == null || params.isEmpty())
        {
            return false;
        }
        try
        {
            return AlipaySignature.rsaCheckV1(params, config.getAlipayPublicKey(), config.getCharset(),
                    config.getSignType());
        }
        catch (AlipayApiException exception)
        {
            throw new AlipayPaymentException("支付宝异步通知验签失败", exception);
        }
    }

    private BigDecimal toAmount(String amount)
    {
        if (amount == null || amount.isBlank())
        {
            return null;
        }
        try
        {
            return new BigDecimal(amount);
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }
}
