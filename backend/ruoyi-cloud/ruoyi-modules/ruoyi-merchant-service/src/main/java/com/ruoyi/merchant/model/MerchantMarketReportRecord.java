package com.ruoyi.merchant.model;

/** 周转市场举报内部记录。 */
public class MerchantMarketReportRecord
{
    private Long reportId;
    private String postNo;
    private String reportedMerchantName;
    private String reportType;
    private String evidence;
    private Integer reportStatus;
    private String reviewResult;
    private String createTime;
    private String reviewTime;

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getPostNo() { return postNo; }
    public void setPostNo(String postNo) { this.postNo = postNo; }
    public String getReportedMerchantName() { return reportedMerchantName; }
    public void setReportedMerchantName(String reportedMerchantName) { this.reportedMerchantName = reportedMerchantName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public Integer getReportStatus() { return reportStatus; }
    public void setReportStatus(Integer reportStatus) { this.reportStatus = reportStatus; }
    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getReviewTime() { return reviewTime; }
    public void setReviewTime(String reviewTime) { this.reviewTime = reviewTime; }
}
