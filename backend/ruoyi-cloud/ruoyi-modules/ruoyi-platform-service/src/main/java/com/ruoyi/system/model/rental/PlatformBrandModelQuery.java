package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 平台标准型号分页列表使用的查询条件。 */
public class PlatformBrandModelQuery
{
    /** 按所属产品类目精确筛选。 */
    @Positive(message = "产品类目ID必须大于0")
    private Long classId;

    /** 按品牌名称模糊筛选。 */
    @Size(max = 64, message = "品牌查询条件长度不能超过64个字符")
    private String brand;

    /** 按型号名称模糊筛选。 */
    @Size(max = 128, message = "型号查询条件长度不能超过128个字符")
    private String modelName;

    /** 按型号状态精确筛选：0正常，1停用。 */
    @Pattern(regexp = "^[01]$", message = "型号状态查询条件不合法")
    private String status;

    /**
     * 获取产品类目筛选条件。
     *
     * @return 产品类目主键，可为空
     */
    public Long getClassId()
    {
        return classId;
    }

    /**
     * 设置产品类目筛选条件。
     *
     * @param classId 产品类目主键
     */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /**
     * 获取品牌模糊查询条件。
     *
     * @return 品牌查询文本，可为空
     */
    public String getBrand()
    {
        return brand;
    }

    /**
     * 设置品牌模糊查询条件。
     *
     * @param brand 品牌查询文本
     */
    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    /**
     * 获取型号名称模糊查询条件。
     *
     * @return 型号名称查询文本，可为空
     */
    public String getModelName()
    {
        return modelName;
    }

    /**
     * 设置型号名称模糊查询条件。
     *
     * @param modelName 型号名称查询文本
     */
    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    /**
     * 获取型号状态筛选条件。
     *
     * @return 0表示正常，1表示停用，可为空
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置型号状态筛选条件。
     *
     * @param status 0表示正常，1表示停用
     */
    public void setStatus(String status)
    {
        this.status = status;
    }
}
