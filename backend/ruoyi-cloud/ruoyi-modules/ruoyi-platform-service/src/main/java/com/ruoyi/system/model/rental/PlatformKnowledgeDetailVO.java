package com.ruoyi.system.model.rental;

import java.util.List;

/** 平台知识文章详情及历史版本。 */
public class PlatformKnowledgeDetailVO
{
    private PlatformKnowledgeArticleVO article;
    private List<PlatformKnowledgeVersionVO> versions;

    public PlatformKnowledgeArticleVO getArticle() { return article; }
    public void setArticle(PlatformKnowledgeArticleVO article) { this.article = article; }
    public List<PlatformKnowledgeVersionVO> getVersions() { return versions; }
    public void setVersions(List<PlatformKnowledgeVersionVO> versions) { this.versions = versions; }
}
