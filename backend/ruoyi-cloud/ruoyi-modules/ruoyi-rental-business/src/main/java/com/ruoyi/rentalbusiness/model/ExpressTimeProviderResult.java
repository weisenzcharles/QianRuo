package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 快递时效供应商统一响应，服务层会进一步解析行政区和映射快递公司编码。 */
public record ExpressTimeProviderResult(boolean available, String providerCompanyCode,
        String fromName, String toName, String expressType, BigDecimal deliveryExpendTime,
        BigDecimal totalTime, BigDecimal remainTime, String message)
{
    /** 构造供应商不可用结果。 */
    public static ExpressTimeProviderResult unavailable(String message)
    {
        return new ExpressTimeProviderResult(false, null, null, null, null, null, null, null, message);
    }
}
