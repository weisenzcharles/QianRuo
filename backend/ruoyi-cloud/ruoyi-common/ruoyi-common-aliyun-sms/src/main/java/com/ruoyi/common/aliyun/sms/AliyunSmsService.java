package com.ruoyi.common.aliyun.sms;

import java.util.Map;

/**
 * 项目内统一的阿里云短信模板发送能力。
 */
public interface AliyunSmsService
{
    /**
     * 向单个手机号发送已审核的短信模板。
     *
     * @param phoneNumber 接收手机号
     * @param signName 已审核的短信签名
     * @param templateCode 已审核的短信模板编码
     * @param templateParameters 模板变量
     * @return 阿里云请求回执
     */
    AliyunSmsReceipt sendTemplateSms(String phoneNumber, String signName, String templateCode,
            Map<String, String> templateParameters);
}
