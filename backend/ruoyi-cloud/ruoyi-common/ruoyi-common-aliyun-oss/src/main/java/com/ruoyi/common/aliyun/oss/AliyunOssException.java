package com.ruoyi.common.aliyun.oss;

/**
 * OSS 操作异常，不包含访问密钥或文件内容。
 */
public class AliyunOssException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public AliyunOssException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AliyunOssException(String message)
    {
        super(message);
    }
}
