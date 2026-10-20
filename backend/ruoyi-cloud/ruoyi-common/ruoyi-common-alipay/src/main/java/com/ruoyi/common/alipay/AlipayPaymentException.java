package com.ruoyi.common.alipay;

/** 支付宝调用或验签失败。 */
public class AlipayPaymentException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public AlipayPaymentException(String message)
    {
        super(message);
    }

    public AlipayPaymentException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
