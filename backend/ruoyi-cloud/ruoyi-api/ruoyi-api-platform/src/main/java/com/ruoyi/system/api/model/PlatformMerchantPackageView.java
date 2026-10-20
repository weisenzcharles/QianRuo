package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台查看商户套餐的只读视图，不包含套餐技术主键。 */
public class PlatformMerchantPackageView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String packageCode;
    private String packageName;
    private String modelName;
    private Integer sourceType;
    private Integer customFlag;
    private Integer versionNo;
    private Integer versionStatus;
    private Integer status;

    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public Integer getCustomFlag() { return customFlag; }
    public void setCustomFlag(Integer customFlag) { this.customFlag = customFlag; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public Integer getVersionStatus() { return versionStatus; }
    public void setVersionStatus(Integer versionStatus) { this.versionStatus = versionStatus; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
