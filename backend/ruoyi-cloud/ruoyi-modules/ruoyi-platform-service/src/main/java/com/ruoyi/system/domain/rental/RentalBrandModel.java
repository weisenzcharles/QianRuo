package com.ruoyi.system.domain.rental;

import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 平台标准型号实体。
 *
 * <p>平台负责维护该实体，商户端只能通过内部只读契约获取有效记录，不能直接新增或修改标准主数据。</p>
 */
public class RentalBrandModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 标准型号唯一编号。 */
    private Long modelId;

    /** 所属平台产品分类唯一编号。 */
    private Long classId;

    /** 所属平台产品分类名称，仅用于列表展示。 */
    private String className;

    /** 所属平台品牌唯一编号。 */
    private Long brandId;

    /** 品牌名称，仅由查询时关联品牌主数据得到，不持久化为型号字段。 */
    private String brand;

    /** 型号名称。 */
    private String modelName;

    /** 型号别名，便于平台和商户使用业务习惯名称检索或展示。 */
    private String alias;

    /** 内存或规格描述。 */
    private String memorySpec;

    /** 是否官方标准：1 是，0 否。 */
    private Integer officialFlag;

    /** 状态：0 正常，1 停用。 */
    private Integer status;

    /** 软删除标识：0 正常，1 删除。 */
    private Integer isDelete;

    /** @return 标准型号唯一编号 */
    public Long getModelId()
    {
        return modelId;
    }

    /** @param modelId 标准型号唯一编号 */
    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    /** @return 所属产品分类唯一编号 */
    public Long getClassId()
    {
        return classId;
    }

    /** @param classId 所属产品分类唯一编号 */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /** @return 所属产品分类名称 */
    public String getClassName()
    {
        return className;
    }

    /** @param className 所属产品分类名称 */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /** @return 所属品牌唯一编号 */
    public Long getBrandId()
    {
        return brandId;
    }

    /** @param brandId 所属品牌唯一编号 */
    public void setBrandId(Long brandId)
    {
        this.brandId = brandId;
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

    /** @return 型号别名 */
    public String getAlias()
    {
        return alias;
    }

    /** @param alias 型号别名 */
    public void setAlias(String alias)
    {
        this.alias = alias;
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
    public Integer getOfficialFlag()
    {
        return officialFlag;
    }

    /** @param officialFlag 是否官方标准 */
    public void setOfficialFlag(Integer officialFlag)
    {
        this.officialFlag = officialFlag;
    }

    /** @return 型号状态 */
    public Integer getStatus()
    {
        return status;
    }

    /** @param status 型号状态 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** @return 软删除标识 */
    public Integer getIsDelete()
    {
        return isDelete;
    }

    /** @param isDelete 软删除标识 */
    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }
}
