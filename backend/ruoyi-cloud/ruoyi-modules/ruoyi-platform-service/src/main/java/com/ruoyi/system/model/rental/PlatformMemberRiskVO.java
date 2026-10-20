package com.ruoyi.system.model.rental;

import java.util.ArrayList;
import java.util.List;

/** 平台商户风险摘要及可追溯记录。 */
public class PlatformMemberRiskVO
{
    private Integer creditScore;
    private Integer creditLevel;
    private String creditLevelName;
    private List<PlatformMemberCreditRecordVO> creditRecords = new ArrayList<>();
    private List<PlatformMemberBlacklistRecordVO> blacklistRecords = new ArrayList<>();

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Integer getCreditLevel() { return creditLevel; }
    public void setCreditLevel(Integer creditLevel) { this.creditLevel = creditLevel; }
    public String getCreditLevelName() { return creditLevelName; }
    public void setCreditLevelName(String creditLevelName) { this.creditLevelName = creditLevelName; }
    public List<PlatformMemberCreditRecordVO> getCreditRecords() { return creditRecords; }
    public void setCreditRecords(List<PlatformMemberCreditRecordVO> creditRecords) { this.creditRecords = creditRecords; }
    public List<PlatformMemberBlacklistRecordVO> getBlacklistRecords() { return blacklistRecords; }
    public void setBlacklistRecords(List<PlatformMemberBlacklistRecordVO> blacklistRecords) { this.blacklistRecords = blacklistRecords; }
}
