package com.ruoyi.merchant.api.model;

import java.util.Date;

/** 联盟商户下拉选项，不返回商户敏感信息。 */
public class MerchantAllianceOption
{
    private Long allianceId;
    private Long memberId;
    private String merchantName;
    private Integer shareStockEnabled;
    private Integer acceptOrderLimit;
    private Integer currentAcceptOrderCount;
    private Integer memberStatus;
    private Date joinTime;
    private Date exitTime;

    public Long getAllianceId() { return allianceId; }
    public void setAllianceId(Long allianceId) { this.allianceId = allianceId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public Integer getShareStockEnabled() { return shareStockEnabled; }
    public void setShareStockEnabled(Integer shareStockEnabled) { this.shareStockEnabled = shareStockEnabled; }
    public Integer getAcceptOrderLimit() { return acceptOrderLimit; }
    public void setAcceptOrderLimit(Integer acceptOrderLimit) { this.acceptOrderLimit = acceptOrderLimit; }
    public Integer getCurrentAcceptOrderCount() { return currentAcceptOrderCount; }
    public void setCurrentAcceptOrderCount(Integer currentAcceptOrderCount) { this.currentAcceptOrderCount = currentAcceptOrderCount; }
    public Integer getMemberStatus() { return memberStatus; }
    public void setMemberStatus(Integer memberStatus) { this.memberStatus = memberStatus; }
    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }
    public Date getExitTime() { return exitTime; }
    public void setExitTime(Date exitTime) { this.exitTime = exitTime; }
}
