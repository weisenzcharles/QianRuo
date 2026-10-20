package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台审核物流赔付单命令。 */
public class PlatformLogisticsClaimReviewCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long claimId;
    private Integer targetStatus;
    private BigDecimal approvedAmount;
    private String reviewRemark;
    private Long operatorId;

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long value) { claimId = value; }
    public Integer getTargetStatus() { return targetStatus; }
    public void setTargetStatus(Integer value) { targetStatus = value; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal value) { approvedAmount = value; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String value) { reviewRemark = value; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long value) { operatorId = value; }
}
