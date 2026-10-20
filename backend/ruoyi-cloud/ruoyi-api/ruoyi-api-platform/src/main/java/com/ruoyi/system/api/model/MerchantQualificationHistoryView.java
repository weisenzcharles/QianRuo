package com.ruoyi.system.api.model;

/** 商户接单资格变更历史跨服务传输模型。 */
public class MerchantQualificationHistoryView
{
    private String qualificationCode;
    private Integer previousStatus;
    private Integer qualificationStatus;
    private String reason;
    private String operatorName;
    private Long operatedAt;

    /** @return 接单资格编码。 */
    public String getQualificationCode() { return qualificationCode; }
    /** @param qualificationCode 设置接单资格编码。 */
    public void setQualificationCode(String qualificationCode) { this.qualificationCode = qualificationCode; }
    /** @return 变更前状态。 */
    public Integer getPreviousStatus() { return previousStatus; }
    /** @param previousStatus 设置变更前状态。 */
    public void setPreviousStatus(Integer previousStatus) { this.previousStatus = previousStatus; }
    /** @return 变更后状态。 */
    public Integer getQualificationStatus() { return qualificationStatus; }
    /** @param qualificationStatus 设置变更后状态。 */
    public void setQualificationStatus(Integer qualificationStatus) { this.qualificationStatus = qualificationStatus; }
    /** @return 变更原因。 */
    public String getReason() { return reason; }
    /** @param reason 设置变更原因。 */
    public void setReason(String reason) { this.reason = reason; }
    /** @return 操作人。 */
    public String getOperatorName() { return operatorName; }
    /** @param operatorName 设置操作人。 */
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    /** @return 操作时间毫秒值。 */
    public Long getOperatedAt() { return operatedAt; }
    /** @param operatedAt 设置操作时间毫秒值。 */
    public void setOperatedAt(Long operatedAt) { this.operatedAt = operatedAt; }
}
