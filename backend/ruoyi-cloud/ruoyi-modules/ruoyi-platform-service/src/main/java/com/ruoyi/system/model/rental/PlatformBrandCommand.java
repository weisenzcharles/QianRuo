package com.ruoyi.system.model.rental;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 平台品牌新增或修改时使用的写入命令。 */
public class PlatformBrandCommand
{
    /** 品牌唯一编号，新增时为空。 */
    @Positive(message = "品牌唯一编号必须大于0")
    private Long brandId;

    /** 品牌名称。 */
    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 64, message = "品牌名称长度不能超过64个字符")
    private String brandName;

    /** 品牌经营范围中的二级产品分类唯一编号集合；每个编号对应关系表中的一行。 */
    @NotEmpty(message = "至少选择一个所属二级产品分类")
    private List<@Positive(message = "所属产品分类唯一编号必须大于0") Long> classIdList = new ArrayList<Long>();

    /** 显示顺序。 */
    private Integer sortNo;

    /** 状态：0正常，1停用。 */
    @Min(value = 0, message = "品牌状态不合法")
    @Max(value = 1, message = "品牌状态不合法")
    private Integer status;

    /** 平台维护备注。 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    public Long getBrandId()
    {
        return brandId;
    }

    public void setBrandId(Long brandId)
    {
        this.brandId = brandId;
    }

    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    public List<Long> getClassIdList()
    {
        return classIdList;
    }

    public void setClassIdList(List<Long> classIdList)
    {
        this.classIdList = classIdList;
    }

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
