package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 平台商户审核命令。 */
public class RentalMemberAuditCommand
{
    @NotBlank(message = "审核结果不能为空")
    private String authStatus;

    @Size(max = 500, message = "审核意见长度不能超过500个字符")
    private String auditRemark;

    /** @return 平台审核结果，仅允许通过或驳回 */
    public String getAuthStatus() { return authStatus; }

    /** @param authStatus 平台审核结果，仅允许通过或驳回 */
    public void setAuthStatus(String authStatus) { this.authStatus = authStatus; }

    /** @return 平台审核意见，驳回时必须填写 */
    public String getAuditRemark() { return auditRemark; }

    /** @param auditRemark 平台审核意见，驳回时必须填写 */
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
}
