package com.ruoyi.system.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 平台统一业务编码规则持久化实体。 */
public class SysCodeRule extends BaseEntity
{
    /** 编码规则主键。 */
    private Long ruleId;

    /** 稳定的编码类型键。 */
    private String codeType;

    /** 编码规则名称。 */
    private String codeName;

    /** 编码前缀。 */
    private String prefix;

    /** 最终编码总长度。 */
    private Integer codeLength;

    /** 已分配的最大数字序号。 */
    private Long currentValue;

    /** 规则状态，0 启用，1 停用。 */
    private Integer status;

    /** 获取编码规则主键。 */
    public Long getRuleId() { return ruleId; }

    /** 设置编码规则主键。 */
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    /** 获取编码类型键。 */
    public String getCodeType() { return codeType; }

    /** 设置编码类型键。 */
    public void setCodeType(String codeType) { this.codeType = codeType; }

    /** 获取规则名称。 */
    public String getCodeName() { return codeName; }

    /** 设置规则名称。 */
    public void setCodeName(String codeName) { this.codeName = codeName; }

    /** 获取编码前缀。 */
    public String getPrefix() { return prefix; }

    /** 设置编码前缀。 */
    public void setPrefix(String prefix) { this.prefix = prefix; }

    /** 获取编码总长度。 */
    public Integer getCodeLength() { return codeLength; }

    /** 设置编码总长度。 */
    public void setCodeLength(Integer codeLength) { this.codeLength = codeLength; }

    /** 获取已分配的最大序号。 */
    public Long getCurrentValue() { return currentValue; }

    /** 设置已分配的最大序号。 */
    public void setCurrentValue(Long currentValue) { this.currentValue = currentValue; }

    /** 获取规则状态。 */
    public Integer getStatus() { return status; }

    /** 设置规则状态。 */
    public void setStatus(Integer status) { this.status = status; }
}
