package com.ruoyi.merchant.model;

import java.util.List;

/** 商户知识文章详情及版本列表。 */
public class MerchantKnowledgeDetailVO
{
    private MerchantKnowledgeArticleVO article;
    private List<MerchantKnowledgeVersionVO> versions;

    public MerchantKnowledgeArticleVO getArticle()
    {
        return article;
    }

    public void setArticle(MerchantKnowledgeArticleVO article)
    {
        this.article = article;
    }

    public List<MerchantKnowledgeVersionVO> getVersions()
    {
        return versions;
    }

    public void setVersions(List<MerchantKnowledgeVersionVO> versions)
    {
        this.versions = versions;
    }
}
