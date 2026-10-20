package com.ruoyi.rentalbusiness.enums;

/** 售后证据类型，与售后证据表的数值字段保持映射。 */
public enum MerchantAfterSalesEvidenceType
{
    IMAGE(1, "图片"),
    VIDEO(2, "视频"),
    FILE(3, "文件"),
    OTHER(9, "其他");

    private final Integer code;
    private final String label;

    MerchantAfterSalesEvidenceType(Integer code, String label)
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

    public static MerchantAfterSalesEvidenceType fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAfterSalesEvidenceType value : values())
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
