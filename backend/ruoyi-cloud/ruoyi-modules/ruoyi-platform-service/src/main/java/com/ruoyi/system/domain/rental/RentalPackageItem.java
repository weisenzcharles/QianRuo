package com.ruoyi.system.domain.rental;

/** 平台套餐 BOM 明细。 */
public class RentalPackageItem
{
    private Long packageItemId;
    private Long packageVersionId;
    private Integer itemRole;
    private Long modelId;
    private Long accessoryId;
    private String modelName;
    private String accessoryName;
    private Integer quantity;
    private Integer requiredFlag;
    private Integer sortNo;
    /** 软删除标识：0正常，1已删除。 */
    private Integer isDelete;
    private Long createBy;
    public Long getPackageItemId() { return packageItemId; }
    public void setPackageItemId(Long value) { packageItemId = value; }
    public Long getPackageVersionId() { return packageVersionId; }
    public void setPackageVersionId(Long value) { packageVersionId = value; }
    public Integer getItemRole() { return itemRole; }
    public void setItemRole(Integer value) { itemRole = value; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long value) { modelId = value; }
    public Long getAccessoryId() { return accessoryId; }
    public void setAccessoryId(Long value) { accessoryId = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
    public String getAccessoryName() { return accessoryName; }
    public void setAccessoryName(String value) { accessoryName = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public Integer getRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(Integer value) { requiredFlag = value; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer value) { sortNo = value; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer value) { isDelete = value; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long value) { createBy = value; }
}
