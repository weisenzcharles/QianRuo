package com.ruoyi.system.domain.rental;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户当前接单资格快照。资格变更历史单独追加，不覆盖历史记录。 */
public class RentalMemberQualification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long qualificationId;
    private Long tenantId;
    private Long memberId;
    private String qualificationCode;
    private String qualificationStatus;
    private Long certificationId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTo;

    private String reviewedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewedTime;

    private String reviewRemark;

    /** @return 资格主键。 */
    public Long getQualificationId() { return qualificationId; }
    /** @param qualificationId 设置资格主键。 */
    public void setQualificationId(Long qualificationId) { this.qualificationId = qualificationId; }
    /** @return 资格所属租户。 */
    public Long getTenantId() { return tenantId; }
    /** @param tenantId 设置资格所属租户。 */
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    /** @return 资格所属商户。 */
    public Long getMemberId() { return memberId; }
    /** @param memberId 设置资格所属商户。 */
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    /** @return 接单资格编码。 */
    public String getQualificationCode() { return qualificationCode; }
    /** @param qualificationCode 设置接单资格编码。 */
    public void setQualificationCode(String qualificationCode) { this.qualificationCode = qualificationCode; }
    /** @return 当前资格状态。 */
    public String getQualificationStatus() { return qualificationStatus; }
    /** @param qualificationStatus 设置当前资格状态。 */
    public void setQualificationStatus(String qualificationStatus) { this.qualificationStatus = qualificationStatus; }
    /** @return 资格审核关联的认证版本。 */
    public Long getCertificationId() { return certificationId; }
    /** @param certificationId 设置资格审核关联的认证版本。 */
    public void setCertificationId(Long certificationId) { this.certificationId = certificationId; }
    /** @return 资格生效时间。 */
    public Date getEffectiveFrom() { return effectiveFrom; }
    /** @param effectiveFrom 设置资格生效时间。 */
    public void setEffectiveFrom(Date effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    /** @return 资格失效时间。 */
    public Date getEffectiveTo() { return effectiveTo; }
    /** @param effectiveTo 设置资格失效时间。 */
    public void setEffectiveTo(Date effectiveTo) { this.effectiveTo = effectiveTo; }
    /** @return 资格审核人。 */
    public String getReviewedBy() { return reviewedBy; }
    /** @param reviewedBy 设置资格审核人。 */
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    /** @return 资格审核时间。 */
    public Date getReviewedTime() { return reviewedTime; }
    /** @param reviewedTime 设置资格审核时间。 */
    public void setReviewedTime(Date reviewedTime) { this.reviewedTime = reviewedTime; }
    /** @return 资格审核意见。 */
    public String getReviewRemark() { return reviewRemark; }
    /** @param reviewRemark 设置资格审核意见。 */
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
}
