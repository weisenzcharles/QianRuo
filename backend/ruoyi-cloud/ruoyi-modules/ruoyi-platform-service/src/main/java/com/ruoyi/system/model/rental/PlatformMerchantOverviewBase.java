package com.ruoyi.system.model.rental;

import java.util.Date;

/** 平台商户经营概况的商户基础信息，字段仅供平台服务内部组装。 */
public class PlatformMerchantOverviewBase
{
    private Long memberId;
    private String memberCode;
    private String nickname;
    private String legalSubjectName;
    private Integer authStatus;
    private Integer status;
    private Date createTime;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getLegalSubjectName() { return legalSubjectName; }
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }
    public Integer getAuthStatus() { return authStatus; }
    public void setAuthStatus(Integer authStatus) { this.authStatus = authStatus; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
