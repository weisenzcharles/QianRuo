package com.ruoyi.system.domain.rental;

import java.math.BigDecimal;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 平台联盟赔付规则模板。 */
public class RentalCompensationRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long ruleId;
    private String ruleName;
    private Integer disputeType;
    private BigDecimal maxAmount;
    private Integer priority;
    private Integer status;
    private Integer isDelete;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public Integer getDisputeType() { return disputeType; }
    public void setDisputeType(Integer disputeType) { this.disputeType = disputeType; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
}
