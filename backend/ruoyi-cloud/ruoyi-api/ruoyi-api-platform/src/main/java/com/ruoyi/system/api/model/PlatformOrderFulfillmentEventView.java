package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台订单履约状态时间线，不向平台端暴露订单内部唯一编号。 */
public class PlatformOrderFulfillmentEventView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String fromStatusLabel;
    private String toStatusLabel;
    private String reason;
    private String operatorName;
    private String createTime;

    public String getFromStatusLabel() { return fromStatusLabel; }
    public void setFromStatusLabel(String fromStatusLabel) { this.fromStatusLabel = fromStatusLabel; }
    public String getToStatusLabel() { return toStatusLabel; }
    public void setToStatusLabel(String toStatusLabel) { this.toStatusLabel = toStatusLabel; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
