package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台查看的联盟订单结算记账。 */
public class PlatformAllianceSettlementView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long settlementId;
    private Long sourceMemberId;
    private Long receiverMemberId;
    private BigDecimal settlementAmount;
    private BigDecimal expressAmount;
    private BigDecimal compensationAmount;
    private Integer settlementStatus;
    private String createTime;
    private String updateTime;
    private String remark;

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long value) { settlementId = value; }
    public Long getSourceMemberId() { return sourceMemberId; }
    public void setSourceMemberId(Long value) { sourceMemberId = value; }
    public Long getReceiverMemberId() { return receiverMemberId; }
    public void setReceiverMemberId(Long value) { receiverMemberId = value; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal value) { settlementAmount = value; }
    public BigDecimal getExpressAmount() { return expressAmount; }
    public void setExpressAmount(BigDecimal value) { expressAmount = value; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal value) { compensationAmount = value; }
    public Integer getSettlementStatus() { return settlementStatus; }
    public void setSettlementStatus(Integer value) { settlementStatus = value; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String value) { updateTime = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
