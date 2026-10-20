package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台已发布套餐明细内部传输对象。 */
public class PlatformPackageItem implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Integer itemRole;
    private Long modelId;
    private Long accessoryId;
    private Integer quantity;
    private Integer requiredFlag;
    private Integer sortNo;
    public Integer getItemRole() { return itemRole; }
    public void setItemRole(Integer value) { itemRole = value; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long value) { modelId = value; }
    public Long getAccessoryId() { return accessoryId; }
    public void setAccessoryId(Long value) { accessoryId = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public Integer getRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(Integer value) { requiredFlag = value; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer value) { sortNo = value; }
}
