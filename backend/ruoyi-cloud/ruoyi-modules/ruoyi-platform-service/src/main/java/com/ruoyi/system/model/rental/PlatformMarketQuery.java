package com.ruoyi.system.model.rental;

/** 平台周转市场帖子和举报查询条件。 */
public class PlatformMarketQuery
{
    private String keyword;
    private Integer boardType;
    private Integer postStatus;
    private Integer reportStatus;
    private String city;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getBoardType() { return boardType; }
    public void setBoardType(Integer boardType) { this.boardType = boardType; }
    public Integer getPostStatus() { return postStatus; }
    public void setPostStatus(Integer postStatus) { this.postStatus = postStatus; }
    public Integer getReportStatus() { return reportStatus; }
    public void setReportStatus(Integer reportStatus) { this.reportStatus = reportStatus; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
