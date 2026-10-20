package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户订单转单请求。转单目标由 transferMode 和 targetMemberId 明确表达。 */
public class MerchantOrderAllianceTransferCommand
{
    public Long orderId;
    public Integer transferMode;
    public Long targetMemberId;
    public BigDecimal settlementPrice;
    public Integer shippingMode;
    public Integer shippingPayer;
    public String idempotencyKey;
    public String remark;
}
