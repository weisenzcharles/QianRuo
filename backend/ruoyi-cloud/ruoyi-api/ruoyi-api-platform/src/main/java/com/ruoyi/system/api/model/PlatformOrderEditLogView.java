package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台订单详情中的字段变更记录。 */
public class PlatformOrderEditLogView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String fieldName;
    private String beforeDisplayValue;
    private String afterDisplayValue;
    private String changeReason;
    private String operatorName;
    private String operatorIp;
    private String createTime;

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getBeforeDisplayValue() { return beforeDisplayValue; }
    public void setBeforeDisplayValue(String beforeDisplayValue) { this.beforeDisplayValue = beforeDisplayValue; }
    public String getAfterDisplayValue() { return afterDisplayValue; }
    public void setAfterDisplayValue(String afterDisplayValue) { this.afterDisplayValue = afterDisplayValue; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getOperatorIp() { return operatorIp; }
    public void setOperatorIp(String operatorIp) { this.operatorIp = operatorIp; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
