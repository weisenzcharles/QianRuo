package com.ruoyi.system.api.model;

/** 商户当前接单资格跨服务传输模型。 */
public class MerchantQualificationView
{
    private String qualificationCode;
    private Integer qualificationStatus;
    private Long effectiveFrom;
    private Long effectiveTo;
    private String reviewedBy;
    private Long reviewedAt;
    private String reviewRemark;

    /** @return 接单资格编码。 */
    public String getQualificationCode() { return qualificationCode; }
    /** @param qualificationCode 设置接单资格编码。 */
    public void setQualificationCode(String qualificationCode) { this.qualificationCode = qualificationCode; }
    /** @return 接单资格状态。 */
    public Integer getQualificationStatus() { return qualificationStatus; }
    /** @param qualificationStatus 设置接单资格状态。 */
    public void setQualificationStatus(Integer qualificationStatus) { this.qualificationStatus = qualificationStatus; }
    /** @return 生效时间毫秒值。 */
    public Long getEffectiveFrom() { return effectiveFrom; }
    /** @param effectiveFrom 设置生效时间毫秒值。 */
    public void setEffectiveFrom(Long effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    /** @return 失效时间毫秒值。 */
    public Long getEffectiveTo() { return effectiveTo; }
    /** @param effectiveTo 设置失效时间毫秒值。 */
    public void setEffectiveTo(Long effectiveTo) { this.effectiveTo = effectiveTo; }
    /** @return 审核人。 */
    public String getReviewedBy() { return reviewedBy; }
    /** @param reviewedBy 设置审核人。 */
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    /** @return 审核时间毫秒值。 */
    public Long getReviewedAt() { return reviewedAt; }
    /** @param reviewedAt 设置审核时间毫秒值。 */
    public void setReviewedAt(Long reviewedAt) { this.reviewedAt = reviewedAt; }
    /** @return 审核意见。 */
    public String getReviewRemark() { return reviewRemark; }
    /** @param reviewRemark 设置审核意见。 */
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
}
