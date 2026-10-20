package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户端快递时效预估响应，只返回业务页面实际需要的字段。 */
public class ExpressTimeEstimateModel
{
    /** 平台统一快递公司编码。 */
    public String expressCompanyCode;

    /** 快递产品类型。 */
    public String expressType;

    /** 预计配送耗时，单位为天。 */
    public BigDecimal deliveryExpendTime;

    /** 预计总耗时，单位为小时。 */
    public BigDecimal totalTime;

    /** 预计剩余耗时，单位为小时。 */
    public BigDecimal remainTime;
}
