package com.ruoyi.common.alipay;

/**
 * 支付宝客户端配置。密钥只从运行时系统配置读取，不允许写入代码或版本库。
 */
public class AlipayClientConfig
{
    private String serverUrl;
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String signType = "RSA2";
    private String charset = "utf-8";
    private String format = "json";
    private String timeoutExpress = "15m";
    private String notifyUrl;
    private String returnUrl;

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getAlipayPublicKey() { return alipayPublicKey; }
    public void setAlipayPublicKey(String alipayPublicKey) { this.alipayPublicKey = alipayPublicKey; }
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
    public String getCharset() { return charset; }
    public void setCharset(String charset) { this.charset = charset; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getTimeoutExpress() { return timeoutExpress; }
    public void setTimeoutExpress(String timeoutExpress) { this.timeoutExpress = timeoutExpress; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }

    /** 校验创建订单和回调验签所需的运行时配置。 */
    public void validate()
    {
        require(serverUrl, "支付宝网关地址");
        require(appId, "支付宝应用编号");
        require(privateKey, "支付宝应用私钥");
        require(alipayPublicKey, "支付宝公钥");
    }

    private void require(String value, String name)
    {
        if (value == null || value.isBlank())
        {
            throw new AlipayPaymentException(name + "未配置");
        }
    }
}
