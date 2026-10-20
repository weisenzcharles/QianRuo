package com.ruoyi.system.model.rental;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 平台配件新增或修改命令。 */
public class PlatformAccessoryCommand
{
    @Positive(message = "配件唯一编号必须大于0")
    private Long accessoryId;
    @Size(max = 64, message = "配件编码长度不能超过64个字符")
    private String accessoryCode;
    @NotBlank(message = "配件名称不能为空")
    @Size(max = 128, message = "配件名称长度不能超过128个字符")
    private String accessoryName;
    @NotNull(message = "所属产品分类不能为空")
    @Positive(message = "产品分类唯一编号必须大于0")
    private Long classId;
    @Positive(message = "品牌唯一编号必须大于0")
    private Long brandId;
    private List<@Positive(message = "产品型号唯一编号必须大于0") Long> modelIdList = new ArrayList<Long>();
    @Size(max = 255, message = "配件规格长度不能超过255个字符")
    private String specification;
    @Size(max = 32, message = "计量单位长度不能超过32个字符")
    private String unit;
    @Size(max = 1000, message = "配件说明长度不能超过1000个字符")
    private String description;
    @NotNull(message = "状态不能为空")
    private Integer status;
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

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
    public List<Long> getModelIdList() { return modelIdList; }
    public void setModelIdList(List<Long> value) { modelIdList = value; }
    public String getSpecification() { return specification; }
    public void setSpecification(String value) { specification = value; }
    public String getUnit() { return unit; }
    public void setUnit(String value) { unit = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
