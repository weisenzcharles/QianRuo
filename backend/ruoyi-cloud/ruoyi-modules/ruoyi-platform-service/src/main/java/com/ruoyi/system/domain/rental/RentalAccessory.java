package com.ruoyi.system.domain.rental;

import java.util.List;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 平台配件主数据实体。 */
public class RentalAccessory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long accessoryId;
    private String accessoryCode;
    private String accessoryName;
    private Long classId;
    private Long brandId;
    private String className;
    private String brandName;
    private String specification;
    private String unit;
    private String description;
    private Integer status;
    /** 软删除标识：0正常，1已删除。 */
    private Integer isDelete;
    private Integer modelCount;
    private List<Long> modelIdList;

    public Long getAccessoryId() { return accessoryId; }
    public void setAccessoryId(Long accessoryId) { this.accessoryId = accessoryId; }
    public String getAccessoryCode() { return accessoryCode; }
    public void setAccessoryCode(String accessoryCode) { this.accessoryCode = accessoryCode; }
    public String getAccessoryName() { return accessoryName; }
    public void setAccessoryName(String accessoryName) { this.accessoryName = accessoryName; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
    public Integer getModelCount() { return modelCount; }
    public void setModelCount(Integer modelCount) { this.modelCount = modelCount; }
    public List<Long> getModelIdList() { return modelIdList; }
    public void setModelIdList(List<Long> modelIdList) { this.modelIdList = modelIdList; }
}
