package com.ruoyi.rentalbusiness.enums;

/** 商户工作任务业务类型。 */
public enum MerchantWorkTaskType
{
    ORDER(1, "订单履约"), INVENTORY(2, "库存管理"), AFTER_SALES(3, "售后处理"), OTHER(9, "其他");

    private final int code;
    private final String label;

    MerchantWorkTaskType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static MerchantWorkTaskType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantWorkTaskType value : values())
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
