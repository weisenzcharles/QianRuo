package com.ruoyi.system.model.rental;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 平台赔付规则模板新增或修改命令。 */
public class PlatformCompensationRuleCommand
{
    private Long ruleId;
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 128, message = "规则名称长度不能超过128个字符")
    private String ruleName;
    @NotNull(message = "纠纷类型不能为空")
    private Integer disputeType;
    @NotNull(message = "赔付上限不能为空")
    @DecimalMin(value = "0.00", message = "赔付上限不能为负数")
    private BigDecimal maxAmount;
    private Integer priority;
    private Integer status;
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long value) { ruleId = value; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String value) { ruleName = value; }
    public Integer getDisputeType() { return disputeType; }
    public void setDisputeType(Integer value) { disputeType = value; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal value) { maxAmount = value; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer value) { priority = value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
