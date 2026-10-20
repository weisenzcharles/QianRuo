package com.ruoyi.merchant.model;

import java.util.List;

/** 商户知识文章视图，不返回租户和商户内部主键。 */
public class MerchantKnowledgeArticleVO
{
    private Long articleId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private Integer sourceType;
    private String sourceTypeName;
    private Integer status;
    private String statusName;
    private Integer currentVersionNo;
    private Integer pinned;
    private Integer audienceType;
    private String audienceTypeName;
    private List<Long> roleIds;
    private String roleNames;
    private Long readCount;
    private String createTime;
    private String updateTime;

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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public Integer getCurrentVersionNo() { return currentVersionNo; }
    public void setCurrentVersionNo(Integer currentVersionNo) { this.currentVersionNo = currentVersionNo; }
    public Integer getPinned() { return pinned; }
    public void setPinned(Integer pinned) { this.pinned = pinned; }
    public Integer getAudienceType() { return audienceType; }
    public void setAudienceType(Integer audienceType) { this.audienceType = audienceType; }
    public String getAudienceTypeName() { return audienceTypeName; }
    public void setAudienceTypeName(String audienceTypeName) { this.audienceTypeName = audienceTypeName; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }
    public Long getReadCount() { return readCount; }
    public void setReadCount(Long readCount) { this.readCount = readCount; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
