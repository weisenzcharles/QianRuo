package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.Size;

/** 平台配件列表查询条件。 */
public class PlatformAccessoryQuery
{
    @Size(max = 128, message = "配件名称查询条件长度不能超过128个字符")
    private String keyword;
    private Integer status;
    private Long classId;
    private Long brandId;
    /** 只查询兼容指定产品型号的配件。 */
    private Long modelId;

    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public Long getClassId() { return classId; }
    public void setClassId(Long value) { classId = value; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long value) { brandId = value; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long value) { modelId = value; }
}
