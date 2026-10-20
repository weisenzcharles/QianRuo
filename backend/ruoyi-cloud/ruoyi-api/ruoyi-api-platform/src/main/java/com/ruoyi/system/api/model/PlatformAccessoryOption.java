package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 平台官方配件供商户套餐选择的只读选项。 */
public class PlatformAccessoryOption implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long accessoryId;
    private String accessoryCode;
    private String accessoryName;
    private Long classId;
    private Long brandId;
    private String specification;
    private String unit;
    private Integer status;
    private List<Long> modelIds = new ArrayList<Long>();

    public Long getAccessoryId() { return accessoryId; }
    public void setAccessoryId(Long value) { accessoryId = value; }
    public String getAccessoryCode() { return accessoryCode; }
    public void setAccessoryCode(String value) { accessoryCode = value; }
    public String getAccessoryName() { return accessoryName; }
    public void setAccessoryName(String value) { accessoryName = value; }
    public Long getClassId() { return classId; }
    public void setClassId(Long value) { classId = value; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long value) { brandId = value; }
    public String getSpecification() { return specification; }
    public void setSpecification(String value) { specification = value; }
    public String getUnit() { return unit; }
    public void setUnit(String value) { unit = value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public List<Long> getModelIds() { return modelIds; }
    public void setModelIds(List<Long> value) { modelIds = value == null ? new ArrayList<Long>() : value; }
}
