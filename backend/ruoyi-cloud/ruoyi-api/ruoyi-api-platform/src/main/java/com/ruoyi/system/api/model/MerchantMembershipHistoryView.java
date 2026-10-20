package com.ruoyi.system.api.model;

/** 商户会员类型变更历史跨服务传输模型。 */
public class MerchantMembershipHistoryView
{
    private Integer previousType;
    private Integer membershipType;
    private String reason;
    private String operatorName;
    private Long operatedAt;

    /** @return 变更前会员类型。 */
    public Integer getPreviousType() { return previousType; }
    /** @param previousType 设置变更前会员类型。 */
    public void setPreviousType(Integer previousType) { this.previousType = previousType; }
    /** @return 变更后会员类型。 */
    public Integer getMembershipType() { return membershipType; }
    /** @param membershipType 设置变更后会员类型。 */
    public void setMembershipType(Integer membershipType) { this.membershipType = membershipType; }
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
