package com.ruoyi.common.aliyun.sms.internal;

import java.util.Map;
import java.util.regex.Pattern;
import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.ruoyi.common.aliyun.sms.AliyunSmsException;
import com.ruoyi.common.aliyun.sms.AliyunSmsReceipt;
import com.ruoyi.common.aliyun.sms.AliyunSmsService;

/**
 * 短信 SDK 的项目内适配器。
 */
public class DefaultAliyunSmsService implements AliyunSmsService
{
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{5,14}$");

    private final Client client;

    public DefaultAliyunSmsService(Client client)
    {
        this.client = client;
    }

    @Override
    public AliyunSmsReceipt sendTemplateSms(String phoneNumber, String signName, String templateCode,
            Map<String, String> templateParameters)
    {
        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber.trim()).matches())
        {
            throw new IllegalArgumentException("A valid E.164 phone number is required");
        }
        if (signName == null || signName.isBlank() || templateCode == null || templateCode.isBlank())
        {
            throw new IllegalArgumentException("SMS sign name and template code are required");
        }

        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber.trim())
                .setSignName(signName.trim())
                .setTemplateCode(templateCode.trim())
                .setTemplateParam(JSON.toJSONString(templateParameters == null ? Map.of() : templateParameters));
        try
        {
            SendSmsResponse response = client.sendSms(request);
            if (response == null || response.body == null || !"OK".equals(response.body.code))
            {
                throw new AliyunSmsException("阿里云短信发送未成功受理");
            }
            return new AliyunSmsReceipt(response.body.requestId, response.body.bizId);
        }
        catch (AliyunSmsException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new AliyunSmsException("阿里云短信请求失败", exception);
        }
    }
}
