package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 平台新增或修改标准型号时使用的写入命令。 */
public class PlatformBrandModelCommand
{
    /** 标准型号主键，新增时为空，修改时必须填写。 */
    @Positive(message = "型号ID必须大于0")
    private Long modelId;

    /** 标准型号所属的平台产品类目主键。 */
    @NotNull(message = "产品类目不能为空")
    @Positive(message = "产品类目ID必须大于0")
    private Long classId;

    /** 平台统一维护的品牌名称。 */
    @NotBlank(message = "品牌不能为空")
    @Size(max = 64, message = "品牌长度不能超过64个字符")
    private String brand;

    /** 平台统一维护的型号名称。 */
    @NotBlank(message = "型号不能为空")
    @Size(max = 128, message = "型号长度不能超过128个字符")
    private String modelName;

    /** 用于区分同型号不同版本的内存或规格描述。 */
    @Size(max = 64, message = "内存或规格长度不能超过64个字符")
    private String memorySpec;

    /** 是否属于平台官方标准型号：1是，0否。 */
    @Pattern(regexp = "^[01]$", message = "官方标准标识不合法")
    private String officialFlag;

    /** 型号状态：0正常，1停用。 */
    @Pattern(regexp = "^[01]$", message = "型号状态不合法")
    private String status;

    /** 平台人员填写的型号维护备注。 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 获取标准型号主键。
     *
     * @return 新增时为空、修改时为目标型号主键
     */
    public Long getModelId()
    {
        return modelId;
    }

    /**
     * 设置需要修改的标准型号主键。
     *
     * @param modelId 标准型号主键
     */
    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    /**
     * 获取标准型号所属产品类目主键。
     *
     * @return 平台产品类目主键
     */
    public Long getClassId()
    {
        return classId;
    }

    /**
     * 设置标准型号所属产品类目主键。
     *
     * @param classId 平台产品类目主键
     */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /**
     * 获取标准品牌名称。
     *
     * @return 去除首尾空白前的品牌名称
     */
    public String getBrand()
    {
        return brand;
    }

    /**
     * 设置标准品牌名称。
     *
     * @param brand 品牌名称
     */
    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    /**
     * 获取标准型号名称。
     *
     * @return 去除首尾空白前的型号名称
     */
    public String getModelName()
    {
        return modelName;
    }

    /**
     * 设置标准型号名称。
     *
     * @param modelName 型号名称
     */
    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    /**
     * 获取型号的内存或规格描述。
     *
     * @return 内存或规格描述，可为空
     */
    public String getMemorySpec()
    {
        return memorySpec;
    }

    /**
     * 设置型号的内存或规格描述。
     *
     * @param memorySpec 内存或规格描述
     */
    public void setMemorySpec(String memorySpec)
    {
        this.memorySpec = memorySpec;
    }

    /**
     * 获取官方标准标识。
     *
     * @return 1表示官方标准，0表示非官方标准
     */
    public String getOfficialFlag()
    {
        return officialFlag;
    }

    /**
     * 设置官方标准标识。
     *
     * @param officialFlag 1表示官方标准，0表示非官方标准
     */
    public void setOfficialFlag(String officialFlag)
    {
        this.officialFlag = officialFlag;
    }

    /**
     * 获取标准型号状态。
     *
     * @return 0表示正常，1表示停用
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置标准型号状态。
     *
     * @param status 0表示正常，1表示停用
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 获取平台维护备注。
     *
     * @return 型号维护备注，可为空
     */
    public String getRemark()
    {
        return remark;
    }

    /**
     * 设置平台维护备注。
     *
     * @param remark 型号维护备注
     */
    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
