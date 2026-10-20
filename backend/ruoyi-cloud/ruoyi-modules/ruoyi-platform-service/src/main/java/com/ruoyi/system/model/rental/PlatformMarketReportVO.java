package com.ruoyi.system.model.rental;

/** 平台周转市场举报视图。数据库和商户主键仅用于接口内部操作。 */
public class PlatformMarketReportVO
{
    private Long reportId;
    private Long postId;
    private String postNo;
    private String reporterName;
    private String reportedMerchantName;
    private String reportType;
    private String evidence;
    private Integer reportStatus;
    private String reportStatusName;
    private String reviewResult;
    private String reviewerName;
    private String createTime;
    private String reviewTime;

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public String getPostNo() { return postNo; }
    public void setPostNo(String postNo) { this.postNo = postNo; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReportedMerchantName() { return reportedMerchantName; }
    public void setReportedMerchantName(String reportedMerchantName) { this.reportedMerchantName = reportedMerchantName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public Integer getReportStatus() { return reportStatus; }
    public void setReportStatus(Integer reportStatus) { this.reportStatus = reportStatus; }
    public String getReportStatusName() { return reportStatusName; }
    public void setReportStatusName(String reportStatusName) { this.reportStatusName = reportStatusName; }
    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getReviewTime() { return reviewTime; }
    public void setReviewTime(String reviewTime) { this.reviewTime = reviewTime; }
}
