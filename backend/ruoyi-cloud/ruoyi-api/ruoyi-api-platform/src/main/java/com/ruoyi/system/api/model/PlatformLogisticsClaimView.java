package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台物流赔付单视图。 */
public class PlatformLogisticsClaimView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long claimId;
    private Long tenantId;
    private Long memberId;
    private Long logisticsId;
    private Long orderId;
    private String claimNo;
    private String orderNo;
    private String merchantName;
    private String merchantCode;
    private String carrier;
    private String trackingNo;
    private Integer claimType;
    private String claimTypeName;
    private Integer claimStatus;
    private String claimStatusName;
    private BigDecimal expectedAmount;
    private BigDecimal approvedAmount;
    private String evidenceJson;
    private String reviewRemark;
    private String financeLedgerNo;
    private Integer settlementRetryCount;
    private String lastSettlementError;
    private String createTime;
    private String updateTime;
    private String remark;

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long value) { claimId = value; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long value) { tenantId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public Long getLogisticsId() { return logisticsId; }
    public void setLogisticsId(Long value) { logisticsId = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getClaimNo() { return claimNo; }
    public void setClaimNo(String value) { claimNo = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String value) { merchantName = value; }
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String value) { merchantCode = value; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String value) { carrier = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { trackingNo = value; }
    public Integer getClaimType() { return claimType; }
    public void setClaimType(Integer value) { claimType = value; }
    public String getClaimTypeName() { return claimTypeName; }
    public void setClaimTypeName(String value) { claimTypeName = value; }
    public Integer getClaimStatus() { return claimStatus; }
    public void setClaimStatus(Integer value) { claimStatus = value; }
    public String getClaimStatusName() { return claimStatusName; }
    public void setClaimStatusName(String value) { claimStatusName = value; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal value) { expectedAmount = value; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal value) { approvedAmount = value; }
    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String value) { evidenceJson = value; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String value) { reviewRemark = value; }
    public String getFinanceLedgerNo() { return financeLedgerNo; }
    public void setFinanceLedgerNo(String value) { financeLedgerNo = value; }
    public Integer getSettlementRetryCount() { return settlementRetryCount; }
    public void setSettlementRetryCount(Integer value) { settlementRetryCount = value; }
    public String getLastSettlementError() { return lastSettlementError; }
    public void setLastSettlementError(String value) { lastSettlementError = value; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String value) { updateTime = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
