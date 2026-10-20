package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** 平台联盟纠纷列表视图。 */
public class PlatformAllianceDisputeView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long disputeId;
    private Long allianceOrderId;
    private Long initiatorMemberId;
    private Long respondentMemberId;
    private String orderNo;
    private String initiatorMerchantName;
    private String respondentMerchantName;
    private Integer disputeType;
    private Integer disputeStatus;
    private BigDecimal claimAmount;
    private BigDecimal awardedAmount;
    private BigDecimal frozenAmount;
    private String description;
    private String resolution;
    private Date mediationDeadline;
    private Date createTime;
    private Date updateTime;
    private List<Map<String, Object>> evidence;
    private Map<String, Object> mediation;

    public Long getDisputeId() { return disputeId; }
    public void setDisputeId(Long value) { disputeId = value; }
    public Long getAllianceOrderId() { return allianceOrderId; }
    public void setAllianceOrderId(Long value) { allianceOrderId = value; }
    public Long getInitiatorMemberId() { return initiatorMemberId; }
    public void setInitiatorMemberId(Long value) { initiatorMemberId = value; }
    public Long getRespondentMemberId() { return respondentMemberId; }
    public void setRespondentMemberId(Long value) { respondentMemberId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getInitiatorMerchantName() { return initiatorMerchantName; }
    public void setInitiatorMerchantName(String value) { initiatorMerchantName = value; }
    public String getRespondentMerchantName() { return respondentMerchantName; }
    public void setRespondentMerchantName(String value) { respondentMerchantName = value; }
    public Integer getDisputeType() { return disputeType; }
    public void setDisputeType(Integer value) { disputeType = value; }
    public Integer getDisputeStatus() { return disputeStatus; }
    public void setDisputeStatus(Integer value) { disputeStatus = value; }
    public BigDecimal getClaimAmount() { return claimAmount; }
    public void setClaimAmount(BigDecimal value) { claimAmount = value; }
    public BigDecimal getAwardedAmount() { return awardedAmount; }
    public void setAwardedAmount(BigDecimal value) { awardedAmount = value; }
    public BigDecimal getFrozenAmount() { return frozenAmount; }
    public void setFrozenAmount(BigDecimal value) { frozenAmount = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getResolution() { return resolution; }
    public void setResolution(String value) { resolution = value; }
    public Date getMediationDeadline() { return mediationDeadline; }
    public void setMediationDeadline(Date value) { mediationDeadline = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date value) { updateTime = value; }
    public List<Map<String, Object>> getEvidence() { return evidence; }
    public void setEvidence(List<Map<String, Object>> value) { evidence = value; }
    public Map<String, Object> getMediation() { return mediation; }
    public void setMediation(Map<String, Object> value) { mediation = value; }
}
