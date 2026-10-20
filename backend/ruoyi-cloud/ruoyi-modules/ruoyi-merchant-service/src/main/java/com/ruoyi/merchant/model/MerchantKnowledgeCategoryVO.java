package com.ruoyi.merchant.model;

/** 商户知识分类视图。分类编号仅用于后续接口操作，不作为页面文案展示。 */
public class MerchantKnowledgeCategoryVO
{
    private Long categoryId;
    private Long parentId;
    private String categoryName;
    private Integer sortNo;
    private Integer status;
    private String statusName;
    private Boolean platformCategory;

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getStatusName()
    {
        return statusName;
    }

    public void setStatusName(String statusName)
    {
        this.statusName = statusName;
    }

    public Boolean getPlatformCategory()
    {
        return platformCategory;
    }

    public void setPlatformCategory(Boolean platformCategory)
    {
        this.platformCategory = platformCategory;
    }
}
