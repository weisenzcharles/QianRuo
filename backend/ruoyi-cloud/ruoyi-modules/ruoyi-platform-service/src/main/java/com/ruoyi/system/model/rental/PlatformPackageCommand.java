package com.ruoyi.system.model.rental;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.ruoyi.system.domain.rental.RentalPackageItem;

/** 平台官方套餐新增或修改命令。 */
public class PlatformPackageCommand
{
    @Positive(message = "套餐唯一编号必须大于0")
    private Long packageId;
    @Size(max = 64, message = "套餐编码长度不能超过64个字符")
    private String packageCode;
    @NotBlank(message = "套餐名称不能为空") @Size(max = 128, message = "套餐名称长度不能超过128个字符")
    private String packageName;
    @NotNull(message = "产品分类不能为空") @Positive(message = "产品分类唯一编号必须大于0")
    private Long classId;
    @NotNull(message = "主产品型号不能为空") @Positive(message = "产品型号唯一编号必须大于0")
    private Long modelId;
    private Integer status;
    private Integer sortNo;
    @Valid @NotEmpty(message = "套餐至少需要一个明细")
    private List<RentalPackageItem> items = new ArrayList<RentalPackageItem>();
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer value) { sortNo = value; }
    public List<RentalPackageItem> getItems() { return items; }
    public void setItems(List<RentalPackageItem> value) { items = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
