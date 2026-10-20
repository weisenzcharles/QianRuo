package com.ruoyi.system.controller;

import org.junit.Test;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.system.domain.SysConfig;

/** 确认短信凭据不会通过平台参数管理响应或日志明文泄露。 */
public class SysConfigControllerTest
{
    @Test
    public void smsCredentialDisplayHidesConfiguredAndMissingValues()
    {
        require("已配置".equals(SysConfigController.maskSmsCredentialForDisplay(
                GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_SECRET, "secret-value")),
                "configured credential must be displayed as a status");
        require("未配置".equals(SysConfigController.maskSmsCredentialForDisplay(
                GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_ID, "")),
                "empty credential must be clearly marked as missing");
        require("dysmsapi.aliyuncs.com".equals(SysConfigController.maskSmsCredentialForDisplay(
                GlobalConfigConstants.ALIYUN_SMS_ENDPOINT, "dysmsapi.aliyuncs.com")),
                "non-sensitive settings must remain visible");
    }

    @Test
    public void editResponseMasksConfiguredCredential()
    {
        require("********".equals(SysConfigController.maskSmsCredentialForEdit(
                GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_SECRET, "secret-value")),
                "edit response must not include the stored credential");
    }

    @Test
    public void editingUnchangedMaskedCredentialRestoresStoredValue()
    {
        SysConfig submitted = config(GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_SECRET, "********");
        SysConfig stored = config(GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_SECRET, "secret-value");

        SysConfigController.restoreMaskedSmsCredential(submitted, stored);

        require("secret-value".equals(submitted.getConfigValue()),
                "unchanged mask must not overwrite the stored credential");
    }

    @Test
    public void configToStringDoesNotContainSmsCredential()
    {
        SysConfig config = config(GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_ID, "access-key-value");

        require(!config.toString().contains("access-key-value"), "config logs must not reveal SMS credentials");
    }

    @Test
    public void alipayCredentialDisplayAndToStringStayMasked()
    {
        require("已配置".equals(SysConfigController.maskCredentialForDisplay(
                GlobalConfigConstants.ALIPAY_PRIVATE_KEY, "private-key")),
                "configured Alipay private key must be displayed as a status");
        require("********".equals(SysConfigController.maskCredentialForEdit(
                GlobalConfigConstants.ALIPAY_PUBLIC_KEY, "public-key")),
                "edit response must not include the stored Alipay public key");
        SysConfig config = config(GlobalConfigConstants.ALIPAY_PRIVATE_KEY, "alipay-private-secret");
        require(!config.toString().contains("alipay-private-secret"), "config logs must not reveal Alipay credentials");
    }

    private SysConfig config(String configKey, String configValue)
    {
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        return config;
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
