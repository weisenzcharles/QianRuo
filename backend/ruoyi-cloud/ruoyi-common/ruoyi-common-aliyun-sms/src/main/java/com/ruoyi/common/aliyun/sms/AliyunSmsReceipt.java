package com.ruoyi.common.aliyun.sms;

/**
 * 阿里云短信受理回执。
 *
 * @param requestId 阿里云请求编号
 * @param businessId 阿里云短信业务编号
 */
public record AliyunSmsReceipt(String requestId, String businessId)
{
}
