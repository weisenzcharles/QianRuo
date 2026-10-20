package com.ruoyi.rentalbusiness.enums;

/** 售后处理事件类型，与售后事件表的数值字段保持映射。 */
public enum MerchantAfterSalesEventType
{
    CREATED(1, "创建售后单"),
    COMPLETED(2, "完成售后单"),
    NOTE(3, "处理记录"),
    EVIDENCE_ADDED(4, "上传证据");

    private final Integer code;
    private final String label;

    MerchantAfterSalesEventType(Integer code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public Integer getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public static MerchantAfterSalesEventType fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAfterSalesEventType value : values())
            {
                if (value.code.equals(code))
                {
                    return value;
                }
            }
        }
        return null;
    }
}
