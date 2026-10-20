package com.ruoyi.system.domain.rental;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户接单资格变更历史。 */
public class RentalMemberQualificationHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long historyId;
    private Long tenantId;
    private Long memberId;
    private Long qualificationId;
    private String qualificationCode;
    private Integer previousStatus;
    private Integer qualificationStatus;
    private Long certificationId;
    private String reason;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatedTime;

    /** @return 资格历史唯一编号。 */
    public Long getHistoryId() { return historyId; }
    /** @param historyId 设置资格历史唯一编号。 */
    public void setHistoryId(Long historyId) { this.historyId = historyId; }
    /** @return 历史记录所属租户。 */
    public Long getTenantId() { return tenantId; }
    /** @param tenantId 设置历史记录所属租户。 */
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    /** @return 历史记录所属商户。 */
    public Long getMemberId() { return memberId; }
    /** @param memberId 设置历史记录所属商户。 */
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    /** @return 当前资格快照唯一编号。 */
    public Long getQualificationId() { return qualificationId; }
    /** @param qualificationId 设置当前资格快照唯一编号。 */
    public void setQualificationId(Long qualificationId) { this.qualificationId = qualificationId; }
    /** @return 资格编码。 */
    public String getQualificationCode() { return qualificationCode; }
    /** @param qualificationCode 设置资格编码。 */
    public void setQualificationCode(String qualificationCode) { this.qualificationCode = qualificationCode; }
    /** @return 变更前资格状态。 */
    public Integer getPreviousStatus() { return previousStatus; }
    /** @param previousStatus 设置变更前资格状态。 */
    public void setPreviousStatus(Integer previousStatus) { this.previousStatus = previousStatus; }
    /** @return 变更后资格状态。 */
    public Integer getQualificationStatus() { return qualificationStatus; }
    /** @param qualificationStatus 设置变更后资格状态。 */
    public void setQualificationStatus(Integer qualificationStatus) { this.qualificationStatus = qualificationStatus; }
    /** @return 关联认证版本。 */
    public Long getCertificationId() { return certificationId; }
    /** @param certificationId 设置关联认证版本。 */
    public void setCertificationId(Long certificationId) { this.certificationId = certificationId; }
    /** @return 资格变更原因。 */
    public String getReason() { return reason; }
    /** @param reason 设置资格变更原因。 */
    public void setReason(String reason) { this.reason = reason; }
    /** @return 执行变更的平台用户。 */
    public String getOperatorName() { return operatorName; }
    /** @param operatorName 设置执行变更的平台用户。 */
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    /** @return 资格变更时间。 */
    public Date getOperatedTime() { return operatedTime; }
    /** @param operatedTime 设置资格变更时间。 */
    public void setOperatedTime(Date operatedTime) { this.operatedTime = operatedTime; }
}
