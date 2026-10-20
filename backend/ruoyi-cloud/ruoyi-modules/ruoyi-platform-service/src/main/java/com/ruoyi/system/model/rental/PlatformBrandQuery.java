package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 平台品牌列表查询条件。 */
public class PlatformBrandQuery
{
    /** 品牌名称模糊查询条件。 */
    @Size(max = 64, message = "品牌名称查询条件长度不能超过64个字符")
    private String brandName;

    /** 品牌状态查询条件：0正常，1停用。 */
    @Min(value = 0, message = "品牌状态查询条件不合法")
    @Max(value = 1, message = "品牌状态查询条件不合法")
    private Integer status;

    /** 按可经营产品分类筛选品牌。 */
    @Positive(message = "产品分类唯一编号必须大于0")
    private Long classId;

    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Long getClassId()
    {
        return classId;
    }

    public void setClassId(Long classId)
    {
        this.classId = classId;
    }
}
