package com.ruoyi.system.api.model;

import java.util.List;

/** 商户服务读取的认证、会员类型和接单资格聚合视图。 */
public class MerchantCertificationView
{
    private Long memberId;
    private String memberType;
    private String membershipType;
    private String authStatus;
    private List<MerchantCertificationVersionView> certificationHistory;
    private List<MerchantQualificationView> qualifications;
    private List<MerchantMembershipHistoryView> membershipHistory;
    private List<MerchantQualificationHistoryView> qualificationHistory;

    /** @return 商户主键。 */
    public Long getMemberId() { return memberId; }
    /** @param memberId 设置商户主键。 */
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    /** @return 商户主体类型。 */
    public String getMemberType() { return memberType; }
    /** @param memberType 设置商户主体类型。 */
    public void setMemberType(String memberType) { this.memberType = memberType; }
    /** @return 当前会员类型。 */
    public String getMembershipType() { return membershipType; }
    /** @param membershipType 设置当前会员类型。 */
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
    /** @return 当前认证状态。 */
    public String getAuthStatus() { return authStatus; }
    /** @param authStatus 设置当前认证状态。 */
    public void setAuthStatus(String authStatus) { this.authStatus = authStatus; }
    /** @return 认证版本历史。 */
    public List<MerchantCertificationVersionView> getCertificationHistory() { return certificationHistory; }
    /** @param certificationHistory 设置认证版本历史。 */
    public void setCertificationHistory(List<MerchantCertificationVersionView> certificationHistory) { this.certificationHistory = certificationHistory; }
    /** @return 当前接单资格快照。 */
    public List<MerchantQualificationView> getQualifications() { return qualifications; }
    /** @param qualifications 设置当前接单资格快照。 */
    public void setQualifications(List<MerchantQualificationView> qualifications) { this.qualifications = qualifications; }
    /** @return 会员类型变更历史。 */
    public List<MerchantMembershipHistoryView> getMembershipHistory() { return membershipHistory; }
    /** @param membershipHistory 设置会员类型变更历史。 */
    public void setMembershipHistory(List<MerchantMembershipHistoryView> membershipHistory) { this.membershipHistory = membershipHistory; }
    /** @return 接单资格变更历史。 */
    public List<MerchantQualificationHistoryView> getQualificationHistory() { return qualificationHistory; }
    /** @param qualificationHistory 设置接单资格变更历史。 */
    public void setQualificationHistory(List<MerchantQualificationHistoryView> qualificationHistory) { this.qualificationHistory = qualificationHistory; }
}
