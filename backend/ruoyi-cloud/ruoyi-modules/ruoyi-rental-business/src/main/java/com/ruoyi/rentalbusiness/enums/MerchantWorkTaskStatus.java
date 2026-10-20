package com.ruoyi.rentalbusiness.enums;

/** 商户工作任务状态。 */
public enum MerchantWorkTaskStatus
{
    PENDING(0, "待处理"), PROCESSING(1, "处理中"), WAITING_REVIEW(2, "待审核"), COMPLETED(3, "已完成"),
    OVERDUE(4, "已逾期"), CLOSED(5, "已关闭");

    private final int code;
    private final String label;

    MerchantWorkTaskStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public static MerchantWorkTaskStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantWorkTaskStatus value : values())
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
