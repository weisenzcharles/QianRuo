package com.ruoyi.system.api.model;

/** 商户入驻申请查询结果，不返回租户和商户主键。 */
public class MerchantRegistrationApplicationVO extends MerchantRegistrationApplicationCommand
{
    private static final long serialVersionUID = 1L;

    private String memberCode;
    private String authStatus;
    private String auditRemark;
    private Long submittedAt;
    private Long updatedAt;
    private String registrationStatus;
    private String paymentNo;
    private java.math.BigDecimal paymentAmount;

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

    /** @return 注册状态 */
    public String getRegistrationStatus() { return registrationStatus; }

    /** @param registrationStatus 注册状态 */
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }

    /** @return 待支付注册单号 */
    public String getPaymentNo() { return paymentNo; }

    /** @param paymentNo 待支付注册单号 */
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }

    /** @return 注册支付金额 */
    public java.math.BigDecimal getPaymentAmount() { return paymentAmount; }

    /** @param paymentAmount 注册支付金额 */
    public void setPaymentAmount(java.math.BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
}
