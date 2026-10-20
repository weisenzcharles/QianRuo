package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户发起联盟订单纠纷请求。 */
public class MerchantAllianceDisputeCreateCommand {
    public Long allianceOrderId;
    public Integer disputeType;
    public BigDecimal claimAmount;
    public String description;
    public String idempotencyKey;
}
