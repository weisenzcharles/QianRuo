package com.ruoyi.merchant.model;

/** 商户可见的周转市场举报视图。 */
public class MerchantMarketReportVO
{
    private String postNo;
    private String reportedMerchantName;
    private String reportType;
    private String reportTypeName;
    private Integer reportStatus;
    private String reportStatusName;
    private String evidence;
    private String reviewResult;
    private String createTime;
    private String reviewTime;

    public String getPostNo() { return postNo; }
    public void setPostNo(String postNo) { this.postNo = postNo; }
    public String getReportedMerchantName() { return reportedMerchantName; }
    public void setReportedMerchantName(String reportedMerchantName) { this.reportedMerchantName = reportedMerchantName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getReportTypeName() { return reportTypeName; }
    public void setReportTypeName(String reportTypeName) { this.reportTypeName = reportTypeName; }
    public Integer getReportStatus() { return reportStatus; }
    public void setReportStatus(Integer reportStatus) { this.reportStatus = reportStatus; }
    public String getReportStatusName() { return reportStatusName; }
    public void setReportStatusName(String reportStatusName) { this.reportStatusName = reportStatusName; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getReviewTime() { return reviewTime; }
    public void setReviewTime(String reviewTime) { this.reviewTime = reviewTime; }
}
