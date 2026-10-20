package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 平台已发布套餐内部传输对象。 */
public class PlatformPackage implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long packageId;
    private String packageCode;
    private String packageName;
    private Long classId;
    private Long modelId;
    private String className;
    private String modelName;
    private Integer versionNo;
    private String bomHash;
    private List<PlatformPackageItem> items = new ArrayList<PlatformPackageItem>();
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
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer value) { versionNo = value; }
    public String getBomHash() { return bomHash; }
    public void setBomHash(String value) { bomHash = value; }
    public List<PlatformPackageItem> getItems() { return items; }
    public void setItems(List<PlatformPackageItem> value) { items = value; }
}
