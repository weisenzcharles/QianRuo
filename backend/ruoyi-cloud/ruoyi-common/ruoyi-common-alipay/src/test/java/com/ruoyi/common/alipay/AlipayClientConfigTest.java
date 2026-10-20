package com.ruoyi.common.alipay;

import org.junit.Assert;
import org.junit.Test;

/** 支付宝运行时配置的最小校验测试。 */
public class AlipayClientConfigTest
{
    @Test
    public void validate_缺少应用私钥_拒绝创建客户端()
    {
        AlipayClientConfig config = new AlipayClientConfig();
        config.setServerUrl("https://openapi.alipay.com/gateway.do");
        config.setAppId("app-id");
        config.setAlipayPublicKey("public-key");

        AlipayPaymentException exception = Assert.assertThrows(AlipayPaymentException.class, config::validate);

        Assert.assertEquals("支付宝应用私钥未配置", exception.getMessage());
    }

    @Test
    public void validate_配置完整_允许创建客户端()
    {
        AlipayClientConfig config = new AlipayClientConfig();
        config.setServerUrl("https://openapi.alipay.com/gateway.do");
        config.setAppId("app-id");
        config.setPrivateKey("private-key");
        config.setAlipayPublicKey("public-key");

        config.validate();
    }
}
