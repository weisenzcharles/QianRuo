package com.ruoyi.rentalbusiness.model;

/** 商户端下拉选项，只返回页面实际需要的名称和值。 */
public class MerchantDictionaryOption
{
    public String label;
    public Integer value;

    public MerchantDictionaryOption(String label, Integer value)
    {
        this.label = label;
        this.value = value;
    }
}
