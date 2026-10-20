package com.ruoyi.rentalbusiness.enums;

/** 商户工作任务来源。 */
public enum MerchantWorkTaskSourceType
{
    MANAGEMENT(1, "管理任务"), PERSONAL(2, "个人任务");

    private final int code;
    private final String label;

    MerchantWorkTaskSourceType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static MerchantWorkTaskSourceType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantWorkTaskSourceType value : values())
        {
            if (value.code == code)
            {
                return value;
            }
        }
        return null;
    }

    public int getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }
}
