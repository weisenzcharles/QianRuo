package com.ruoyi.system.model.rental;

/** 平台处理市场举报的参数。resolved 为已处理，rejected 为驳回；违规时可联动下架帖子。 */
public class PlatformMarketReportReviewCommand
{
    private Integer reportStatus;
    private String reviewResult;
    private Boolean offlinePost;

    public Integer getReportStatus() { return reportStatus; }
    public void setReportStatus(Integer reportStatus) { this.reportStatus = reportStatus; }
    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }
    public Boolean getOfflinePost() { return offlinePost; }
    public void setOfflinePost(Boolean offlinePost) { this.offlinePost = offlinePost; }
}
