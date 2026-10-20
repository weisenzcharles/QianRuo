package com.ruoyi.system.domain.rental;

import java.util.List;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 平台官方套餐实体。 */
public class RentalPackage extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long packageId;
    private String packageCode;
    private String packageName;
    private Long classId;
    private Long modelId;
    private String className;
    private String modelName;
    private Integer status;
    /** 软删除标识：0正常，1已删除。 */
    private Integer isDelete;
    private Integer sortNo;
    private Long packageVersionId;
    private Integer versionNo;
    private Integer versionStatus;
    private String bomHash;
    private List<RentalPackageItem> items;
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long value) { packageId = value; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String value) { packageCode = value; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String value) { packageName = value; }
    public Long getClassId() { return classId; }
    public void setClassId(Long value) { classId = value; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long value) { modelId = value; }
    public String getClassName() { return className; }
    public void setClassName(String value) { className = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer value) { isDelete = value; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer value) { sortNo = value; }
    public Long getPackageVersionId() { return packageVersionId; }
    public void setPackageVersionId(Long value) { packageVersionId = value; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer value) { versionNo = value; }
    public Integer getVersionStatus() { return versionStatus; }
    public void setVersionStatus(Integer value) { versionStatus = value; }
    public String getBomHash() { return bomHash; }
    public void setBomHash(String value) { bomHash = value; }
    public List<RentalPackageItem> getItems() { return items; }
    public void setItems(List<RentalPackageItem> value) { items = value; }
}
