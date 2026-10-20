package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户物流赔付单视图。 */
public class MerchantLogisticsClaimVO
{
    public Long claimId;
    public Long logisticsId;
    public Long orderId;
    public String claimNo;
    public String orderNo;
    public String carrier;
    public String trackingNo;
    public Integer claimType;
    public String claimTypeName;
    public Integer claimStatus;
    public String claimStatusName;
    public BigDecimal expectedAmount;
    public BigDecimal approvedAmount;
    public String evidenceJson;
    public String reviewRemark;
    public String financeLedgerNo;
    public Integer settlementRetryCount;
    public String lastSettlementError;
    public String createTime;
    public String updateTime;
    public String remark;
}
