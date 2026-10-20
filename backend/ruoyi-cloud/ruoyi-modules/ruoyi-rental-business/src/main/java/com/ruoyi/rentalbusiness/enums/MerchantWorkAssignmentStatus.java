package com.ruoyi.rentalbusiness.enums;

/** 商户工作任务指派状态。 */
public enum MerchantWorkAssignmentStatus
{
    PENDING(0, "待处理"), PROCESSING(1, "处理中"), SUBMITTED(2, "已提交"), COMPLETED(3, "已完成"), REJECTED(4, "已驳回");

    private final int code;
    private final String label;

    MerchantWorkAssignmentStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static MerchantWorkAssignmentStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantWorkAssignmentStatus value : values())
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
