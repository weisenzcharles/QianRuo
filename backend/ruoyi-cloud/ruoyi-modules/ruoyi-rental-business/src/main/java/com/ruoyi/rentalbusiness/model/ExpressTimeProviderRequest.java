package com.ruoyi.rentalbusiness.model;

import java.time.LocalDateTime;

/** 快递时效供应商统一请求。 */
public record ExpressTimeProviderRequest(String fromAddress, String toAddress,
        String providerCompanyCode, String expressType, LocalDateTime orderTime)
{
    /** 校验并标准化供应商请求参数。 */
    public ExpressTimeProviderRequest normalized()
    {
        if (isBlank(fromAddress) || isBlank(toAddress))
        {
            throw new IllegalArgumentException("寄件地址和收件地址不能为空");
        }
        if (isBlank(providerCompanyCode))
        {
            throw new IllegalArgumentException("缺少供应商快递公司编码");
        }
        return new ExpressTimeProviderRequest(fromAddress.trim(), toAddress.trim(),
                providerCompanyCode.trim(), trim(expressType), orderTime == null ? LocalDateTime.now() : orderTime);
    }

    private static String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
