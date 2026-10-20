package com.ruoyi.system.api.model;

import java.util.ArrayList;
import java.util.List;

/** 商户端信用摘要和可申诉流水。 */
public class MerchantCreditRiskView
{
    private Integer creditScore;
    private Integer creditLevel;
    private String creditLevelName;
    private List<MerchantCreditRecordView> creditRecords = new ArrayList<>();

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Integer getCreditLevel() { return creditLevel; }
    public void setCreditLevel(Integer creditLevel) { this.creditLevel = creditLevel; }
    public String getCreditLevelName() { return creditLevelName; }
    public void setCreditLevelName(String creditLevelName) { this.creditLevelName = creditLevelName; }
    public List<MerchantCreditRecordView> getCreditRecords() { return creditRecords; }
    public void setCreditRecords(List<MerchantCreditRecordView> creditRecords) { this.creditRecords = creditRecords; }
}
