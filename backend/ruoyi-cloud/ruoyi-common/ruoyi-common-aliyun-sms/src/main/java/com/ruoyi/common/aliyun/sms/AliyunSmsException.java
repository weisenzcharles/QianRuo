package com.ruoyi.common.aliyun.sms;

/**
 * 短信请求异常，不包含手机号或模板变量内容。
 */
public class AliyunSmsException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public AliyunSmsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AliyunSmsException(String message)
    {
        super(message);
    }
}
