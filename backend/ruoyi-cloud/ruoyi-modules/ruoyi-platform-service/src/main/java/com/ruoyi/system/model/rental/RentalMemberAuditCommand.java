package com.ruoyi.system.model.rental;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 平台商户审核命令。 */
public class RentalMemberAuditCommand
{
    @NotBlank(message = "审核结果不能为空")
    private String authStatus;

    @Size(max = 500, message = "审核意见长度不能超过500个字符")
    private String auditRemark;

    @Size(max = 32, message = "会员类型编码长度不能超过32个字符")
    private String membershipType;

    private List<String> qualificationCodes;

    /** @return 平台审核结果，仅允许通过或驳回 */
    public String getAuthStatus() { return authStatus; }

    /** @param authStatus 平台审核结果，仅允许通过或驳回 */
    public void setAuthStatus(String authStatus) { this.authStatus = authStatus; }

    /** @return 平台审核意见，驳回时必须填写 */
    public String getAuditRemark() { return auditRemark; }

    /** @param auditRemark 平台审核意见，驳回时必须填写 */
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    /**
     * 获取审核通过后生效的会员类型。审核驳回时该字段不会改变商户当前类型。
     *
     * @return 会员类型编码
     */
    public String getMembershipType() { return membershipType; }

    /**
     * 设置审核通过后生效的会员类型。
     *
     * @param membershipType 会员类型编码
     */
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }

    /**
     * 获取审核通过后允许开通的接单资格编码集合。未传入时保留当前资格快照。
     *
     * @return 接单资格编码集合
     */
    public List<String> getQualificationCodes() { return qualificationCodes; }

    /**
     * 设置审核通过后允许开通的接单资格编码集合。
     *
     * @param qualificationCodes 接单资格编码集合
     */
    public void setQualificationCodes(List<String> qualificationCodes) { this.qualificationCodes = qualificationCodes; }
}
