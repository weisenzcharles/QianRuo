package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 当前商户邀请码和邀请结果汇总。 */
public class MerchantInvitationOverviewVO implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String inviteCode;
    private String levelName;
    private Integer inviteLimit;
    private Integer usedCount;
    private Integer remainingCount;
    private List<MerchantInvitationRecordVO> invitations = new ArrayList<>();

    /** @return 邀请码 */
    public String getInviteCode() { return inviteCode; }
    /** @param inviteCode 邀请码 */
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    /** @return 当前等级名称 */
    public String getLevelName() { return levelName; }
    /** @param levelName 当前等级名称 */
    public void setLevelName(String levelName) { this.levelName = levelName; }
    /** @return 邀请名额 */
    public Integer getInviteLimit() { return inviteLimit; }
    /** @param inviteLimit 邀请名额 */
    public void setInviteLimit(Integer inviteLimit) { this.inviteLimit = inviteLimit; }
    /** @return 已邀请人数 */
    public Integer getUsedCount() { return usedCount; }
    /** @param usedCount 已邀请人数 */
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    /** @return 剩余邀请名额 */
    public Integer getRemainingCount() { return remainingCount; }
    /** @param remainingCount 剩余邀请名额 */
    public void setRemainingCount(Integer remainingCount) { this.remainingCount = remainingCount; }
    /** @return 邀请记录 */
    public List<MerchantInvitationRecordVO> getInvitations() { return invitations; }
    /** @param invitations 邀请记录 */
    public void setInvitations(List<MerchantInvitationRecordVO> invitations) { this.invitations = invitations; }
}
