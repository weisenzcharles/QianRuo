package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 商户邀请记录视图。 */
public class MerchantInvitationRecordVO implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long invitationId;
    private String inviteeMemberCode;
    private String inviteeName;
    private String inviteePhone;
    private Integer inviteStatus;
    private String registeredTime;

    /** @return 邀请记录ID */
    public Long getInvitationId() { return invitationId; }
    /** @param invitationId 邀请记录ID */
    public void setInvitationId(Long invitationId) { this.invitationId = invitationId; }
    /** @return 被邀请商户编码 */
    public String getInviteeMemberCode() { return inviteeMemberCode; }
    /** @param inviteeMemberCode 被邀请商户编码 */
    public void setInviteeMemberCode(String inviteeMemberCode) { this.inviteeMemberCode = inviteeMemberCode; }
    /** @return 被邀请商户名称 */
    public String getInviteeName() { return inviteeName; }
    /** @param inviteeName 被邀请商户名称 */
    public void setInviteeName(String inviteeName) { this.inviteeName = inviteeName; }
    /** @return 被邀请商户手机号 */
    public String getInviteePhone() { return inviteePhone; }
    /** @param inviteePhone 被邀请商户手机号 */
    public void setInviteePhone(String inviteePhone) { this.inviteePhone = inviteePhone; }
    /** @return 邀请状态 */
    public Integer getInviteStatus() { return inviteStatus; }
    /** @param inviteStatus 邀请状态 */
    public void setInviteStatus(Integer inviteStatus) { this.inviteStatus = inviteStatus; }
    /** @return 注册时间 */
    public String getRegisteredTime() { return registeredTime; }
    /** @param registeredTime 注册时间 */
    public void setRegisteredTime(String registeredTime) { this.registeredTime = registeredTime; }
}
