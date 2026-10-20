package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台发起联盟纠纷调解方案请求。 */
public class PlatformAllianceMediationCommand implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long disputeId;
    private BigDecimal proposalAmount;
    private String proposalContent;

    public Long getDisputeId() { return disputeId; }
    public void setDisputeId(Long value) { disputeId = value; }
    public BigDecimal getProposalAmount() { return proposalAmount; }
    public void setProposalAmount(BigDecimal value) { proposalAmount = value; }
    public String getProposalContent() { return proposalContent; }
    public void setProposalContent(String value) { proposalContent = value; }
}
