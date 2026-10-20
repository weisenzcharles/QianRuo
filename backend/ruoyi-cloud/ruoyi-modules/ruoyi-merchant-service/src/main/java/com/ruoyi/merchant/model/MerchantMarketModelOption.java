package com.ruoyi.merchant.model;

/** 平台有效标准型号的市场选择项。 */
public class MerchantMarketModelOption
{
    private String category;
    private String brand;
    private String modelName;
    private String memorySpec;

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getBrand()
    {
        return brand;
    }

    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getMemorySpec()
    {
        return memorySpec;
    }

    public void setMemorySpec(String memorySpec)
    {
        this.memorySpec = memorySpec;
    }
}
