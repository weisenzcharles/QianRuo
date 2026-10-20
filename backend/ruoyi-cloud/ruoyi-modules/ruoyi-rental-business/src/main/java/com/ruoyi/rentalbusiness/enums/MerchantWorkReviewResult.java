package com.ruoyi.rentalbusiness.enums;

/** 商户工作任务质检结果。 */
public enum MerchantWorkReviewResult
{
    PASSED(1, "质检通过"), FAILED(2, "质检未达标");

    private final int code;
    private final String label;

    MerchantWorkReviewResult(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static MerchantWorkReviewResult fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantWorkReviewResult value : values())
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
