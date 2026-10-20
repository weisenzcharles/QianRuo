package com.ruoyi.rentalbusiness.domain;

import java.math.BigDecimal;

/** 待向商户财务服务入账的物流赔付投影。 */
public class LogisticsClaimSettlementCandidate
{
    public Long claimId;
    public Long tenantId;
    public Long memberId;
    public String claimNo;
    public BigDecimal approvedAmount;
}
