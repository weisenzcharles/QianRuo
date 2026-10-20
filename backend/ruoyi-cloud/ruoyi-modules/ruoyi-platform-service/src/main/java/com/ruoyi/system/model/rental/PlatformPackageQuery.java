package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.Size;

/** 平台套餐列表查询条件。 */
public class PlatformPackageQuery
{
    @Size(max = 128, message = "套餐关键词长度不能超过128个字符")
    private String keyword;
    private Integer status;
    private Long classId;
    private Long modelId;
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public Long getClassId() { return classId; }
    public void setClassId(Long value) { classId = value; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long value) { modelId = value; }
}
