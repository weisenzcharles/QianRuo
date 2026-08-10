package com.ruoyi.system.api.model;

import java.io.Serializable;

/**
 * 平台标准品牌型号内部传输对象。
 *
 * <p>该对象用于平台服务向商户服务发布有效的标准型号，不包含平台数据库审计字段，
 * 商户服务只能依据该对象建立当前商户的常用型号关系。</p>
 */
public class BrandModel implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 标准型号主键。 */
    private Long modelId;

    /** 所属平台产品类目主键。 */
    private Long classId;

    /** 所属平台产品类目名称。 */
    private String className;

    /** 品牌名称。 */
    private String brand;

    /** 型号名称。 */
    private String modelName;

    /** 内存或规格描述。 */
    private String memorySpec;

    /** 是否官方标准：1 是，0 否。 */
    private String officialFlag;

    /** 状态：0 正常，1 停用。 */
    private String status;

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

    /** @return 所属平台产品类目主键 */
    public Long getClassId()
    {
        return classId;
    }

    /** @param classId 所属平台产品类目主键 */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /** @return 所属平台产品类目名称 */
    public String getClassName()
    {
        return className;
    }

    /** @param className 所属平台产品类目名称 */
    public void setClassName(String className)
    {
        this.className = className;
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
}
