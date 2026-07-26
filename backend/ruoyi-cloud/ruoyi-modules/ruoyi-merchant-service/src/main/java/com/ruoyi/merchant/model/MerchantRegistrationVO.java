package com.ruoyi.merchant.model;

/** 商户公开查询到的入驻申请。 */
public class MerchantRegistrationVO extends MerchantRegistrationCommand
{
    private String memberCode;
    private String authStatus;
    private String auditRemark;
    private Long submittedAt;
    private Long updatedAt;

    /** @return 平台生成的商户申请编码 */
    public String getMemberCode() { return memberCode; }

    /** @param memberCode 平台生成的商户申请编码 */
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }

    /** @return 当前申请审核状态 */
    public String getAuthStatus() { return authStatus; }

    /** @param authStatus 当前申请审核状态 */
    public void setAuthStatus(String authStatus) { this.authStatus = authStatus; }

    /** @return 平台审核意见或驳回原因 */
    public String getAuditRemark() { return auditRemark; }

    /** @param auditRemark 平台审核意见或驳回原因 */
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    /** @return 首次提交时间的毫秒值 */
    public Long getSubmittedAt() { return submittedAt; }

    /** @param submittedAt 首次提交时间的毫秒值 */
    public void setSubmittedAt(Long submittedAt) { this.submittedAt = submittedAt; }

    /** @return 最近修改时间的毫秒值 */
    public Long getUpdatedAt() { return updatedAt; }

    /** @param updatedAt 最近修改时间的毫秒值 */
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
