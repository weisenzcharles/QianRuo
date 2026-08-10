package com.ruoyi.merchant.model;

/** 商户常用型号页面展示对象。 */
public class MerchantBrandModelVO
{
    /** 标准型号主键。 */
    private Long modelId;

    /** 所属产品类目主键。 */
    private Long classId;

    /** 所属产品类目名称。 */
    private String className;

    /** 品牌名称。 */
    private String brand;

    /** 型号名称。 */
    private String modelName;

    /** 内存或规格描述。 */
    private String memorySpec;

    /** 当前商户是否已加入常用型号。 */
    private boolean selected;

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

    /** @return 产品类目主键 */
    public Long getClassId()
    {
        return classId;
    }

    /** @param classId 产品类目主键 */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /** @return 产品类目名称 */
    public String getClassName()
    {
        return className;
    }

    /** @param className 产品类目名称 */
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

    /** @return 是否已选择为常用型号 */
    public boolean isSelected()
    {
        return selected;
    }

    /** @param selected 是否已选择为常用型号 */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
}
