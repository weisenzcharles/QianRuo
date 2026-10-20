package com.ruoyi.merchant.model;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户知识文章列表查询条件。数据范围由当前商户登录态确定。 */
public class MerchantKnowledgeArticleQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String title;
    private Long categoryId;
    private Integer status;
    private Integer sourceType;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(Integer sourceType)
    {
        this.sourceType = sourceType;
    }
}
