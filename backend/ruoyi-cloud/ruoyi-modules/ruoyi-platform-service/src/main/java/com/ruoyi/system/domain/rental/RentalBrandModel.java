package com.ruoyi.system.domain.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 平台标准品牌型号实体。
 *
 * <p>平台负责维护该实体，商户端只能通过内部只读契约获取有效记录，不能直接新增或修改标准主数据。</p>
 */
public class RentalBrandModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 标准型号主键。 */
    private Long modelId;

    /** 所属平台产品类目主键。 */
    @NotNull(message = "产品类目不能为空")
    private Long classId;

    /** 所属平台产品类目名称，仅用于列表展示。 */
    private String className;

    /** 兼容旧数据的一级分类名称。 */
    private String categoryLevel1;

    /** 兼容旧数据的二级分类名称。 */
    private String categoryLevel2;

    /** 兼容旧数据的三级分类名称。 */
    private String categoryLevel3;

    /** 品牌名称。 */
    @NotBlank(message = "品牌不能为空")
    private String brand;

    /** 型号名称。 */
    @NotBlank(message = "型号不能为空")
    private String modelName;

    /** 内存或规格描述。 */
    private String memorySpec;

    /** 是否官方标准：1 是，0 否。 */
    private String officialFlag;

    /** 状态：0 正常，1 停用。 */
    private String status;

    /** 软删除标识：0 正常，1 删除。 */
    private Integer isdeleted;

    /** @return 标准型号主键 */
    public Long getModelId()
    {
        return modelId;
    }

    /** @param modelId 标准型号主键 */
    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    /** @return 所属产品类目主键 */
    public Long getClassId()
    {
        return classId;
    }

    /** @param classId 所属产品类目主键 */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /** @return 所属产品类目名称 */
    public String getClassName()
    {
        return className;
    }

    /** @param className 所属产品类目名称 */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /** @return 一级分类名称 */
    public String getCategoryLevel1()
    {
        return categoryLevel1;
    }

    /** @param categoryLevel1 一级分类名称 */
    public void setCategoryLevel1(String categoryLevel1)
    {
        this.categoryLevel1 = categoryLevel1;
    }

    /** @return 二级分类名称 */
    public String getCategoryLevel2()
    {
        return categoryLevel2;
    }

    /** @param categoryLevel2 二级分类名称 */
    public void setCategoryLevel2(String categoryLevel2)
    {
        this.categoryLevel2 = categoryLevel2;
    }

    /** @return 三级分类名称 */
    public String getCategoryLevel3()
    {
        return categoryLevel3;
    }

    /** @param categoryLevel3 三级分类名称 */
    public void setCategoryLevel3(String categoryLevel3)
    {
        this.categoryLevel3 = categoryLevel3;
    }

    /** @return 品牌名称 */
    public String getBrand()
    {
        return brand;
    }

    /** @param brand 品牌名称 */
    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    /** @return 型号名称 */
    public String getModelName()
    {
        return modelName;
    }

    /** @param modelName 型号名称 */
    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    /** @return 内存或规格描述 */
    public String getMemorySpec()
    {
        return memorySpec;
    }

    /** @param memorySpec 内存或规格描述 */
    public void setMemorySpec(String memorySpec)
    {
        this.memorySpec = memorySpec;
    }

    /** @return 是否官方标准 */
    public String getOfficialFlag()
    {
        return officialFlag;
    }

    /** @param officialFlag 是否官方标准 */
    public void setOfficialFlag(String officialFlag)
    {
        this.officialFlag = officialFlag;
    }

    /** @return 型号状态 */
    public String getStatus()
    {
        return status;
    }

    /** @param status 型号状态 */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /** @return 软删除标识 */
    public Integer getIsdeleted()
    {
        return isdeleted;
    }

    /** @param isdeleted 软删除标识 */
    public void setIsdeleted(Integer isdeleted)
    {
        this.isdeleted = isdeleted;
    }
}
