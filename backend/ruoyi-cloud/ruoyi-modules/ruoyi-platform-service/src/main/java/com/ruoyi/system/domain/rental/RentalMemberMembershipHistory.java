package com.ruoyi.system.domain.rental;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户会员类型变更历史。 */
public class RentalMemberMembershipHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long historyId;
    private Long tenantId;
    private Long memberId;
    private String previousType;
    private String membershipType;
    private String reason;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatedTime;

    /** @return 会员类型历史主键。 */
    public Long getHistoryId() { return historyId; }
    /** @param historyId 设置会员类型历史主键。 */
    public void setHistoryId(Long historyId) { this.historyId = historyId; }
    /** @return 历史记录所属租户。 */
    public Long getTenantId() { return tenantId; }
    /** @param tenantId 设置历史记录所属租户。 */
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    /** @return 历史记录所属商户。 */
    public Long getMemberId() { return memberId; }
    /** @param memberId 设置历史记录所属商户。 */
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    /** @return 变更前会员类型。 */
    public String getPreviousType() { return previousType; }
    /** @param previousType 设置变更前会员类型。 */
    public void setPreviousType(String previousType) { this.previousType = previousType; }
    /** @return 变更后会员类型。 */
    public String getMembershipType() { return membershipType; }
    /** @param membershipType 设置变更后会员类型。 */
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
    /** @return 变更原因。 */
    public String getReason() { return reason; }
    /** @param reason 设置变更原因。 */
    public void setReason(String reason) { this.reason = reason; }
    /** @return 执行变更的平台用户。 */
    public String getOperatorName() { return operatorName; }
    /** @param operatorName 设置执行变更的平台用户。 */
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    /** @return 会员类型变更时间。 */
    public Date getOperatedTime() { return operatedTime; }
    /** @param operatedTime 设置会员类型变更时间。 */
    public void setOperatedTime(Date operatedTime) { this.operatedTime = operatedTime; }
}
