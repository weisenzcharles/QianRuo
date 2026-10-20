package com.ruoyi.merchant.model;

/** 提交周转市场举报命令。 */
public class MerchantMarketReportCommand
{
    private String postNo;
    private MerchantMarketReportType reportType;
    private String evidence;

    public String getPostNo() { return postNo; }
    public void setPostNo(String postNo) { this.postNo = postNo; }
    public MerchantMarketReportType getReportType() { return reportType; }
    public void setReportType(MerchantMarketReportType reportType) { this.reportType = reportType; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
}
