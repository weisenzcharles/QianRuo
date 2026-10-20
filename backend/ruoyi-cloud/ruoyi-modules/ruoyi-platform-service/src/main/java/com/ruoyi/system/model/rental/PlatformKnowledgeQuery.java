package com.ruoyi.system.model.rental;

/** 平台知识库文章查询条件。 */
public class PlatformKnowledgeQuery
{
    private String title;
    private Long categoryId;
    private Integer status;
    private Integer sourceType;
    private String memberKeyword;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public String getMemberKeyword() { return memberKeyword; }
    public void setMemberKeyword(String memberKeyword) { this.memberKeyword = memberKeyword; }
}
