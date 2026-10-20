package com.ruoyi.merchant.model;

import java.util.List;

/** 商户自建知识文章新增或修改参数。 */
public class MerchantKnowledgeArticleCommand
{
    private Long articleId;
    private Long categoryId;
    private String title;
    private String content;
    private Integer pinned;
    private Integer audienceType;
    private List<Long> roleIds;
    private List<MerchantKnowledgeAttachmentCommand> attachments;

    public Long getArticleId()
    {
        return articleId;
    }

    public void setArticleId(Long articleId)
    {
        this.articleId = articleId;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Integer getPinned()
    {
        return pinned;
    }

    public void setPinned(Integer pinned)
    {
        this.pinned = pinned;
    }

    public Integer getAudienceType() { return audienceType; }
    public void setAudienceType(Integer audienceType) { this.audienceType = audienceType; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    public List<MerchantKnowledgeAttachmentCommand> getAttachments() { return attachments; }
    public void setAttachments(List<MerchantKnowledgeAttachmentCommand> attachments) { this.attachments = attachments; }
}
