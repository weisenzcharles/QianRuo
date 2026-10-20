package com.ruoyi.system.model.rental;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 平台知识文章新增和修改入参。每次修改都会追加一个版本。 */
public class PlatformKnowledgeArticleCommand
{
    private Long articleId;
    @NotNull(message = "知识分类不能为空")
    private Long categoryId;
    @NotBlank(message = "知识标题不能为空")
    @Size(max = 160, message = "知识标题不能超过160个字符")
    private String title;
    @NotBlank(message = "知识正文不能为空")
    private String content;
    private Integer pinned;
    private List<PlatformKnowledgeAttachmentCommand> attachments;

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getPinned() { return pinned; }
    public void setPinned(Integer pinned) { this.pinned = pinned; }
    public List<PlatformKnowledgeAttachmentCommand> getAttachments() { return attachments; }
    public void setAttachments(List<PlatformKnowledgeAttachmentCommand> attachments) { this.attachments = attachments; }
}
