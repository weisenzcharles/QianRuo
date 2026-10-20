package com.ruoyi.system.model.rental;

/** 平台知识文章列表视图。 */
public class PlatformKnowledgeArticleVO
{
    private Long articleId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private Integer sourceType;
    private String sourceTypeName;
    private Long memberId;
    private String memberName;
    private Integer status;
    private String statusName;
    private Integer currentVersionNo;
    private Integer pinned;
    private Integer readCount;
    private String createTime;
    private String updateTime;
    private String violationReason;
    private String violationTime;

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public String getSourceTypeName() { return sourceTypeName; }
    public void setSourceTypeName(String sourceTypeName) { this.sourceTypeName = sourceTypeName; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public Integer getCurrentVersionNo() { return currentVersionNo; }
    public void setCurrentVersionNo(Integer currentVersionNo) { this.currentVersionNo = currentVersionNo; }
    public Integer getPinned() { return pinned; }
    public void setPinned(Integer pinned) { this.pinned = pinned; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getViolationReason() { return violationReason; }
    public void setViolationReason(String violationReason) { this.violationReason = violationReason; }
    public String getViolationTime() { return violationTime; }
    public void setViolationTime(String violationTime) { this.violationTime = violationTime; }
}
