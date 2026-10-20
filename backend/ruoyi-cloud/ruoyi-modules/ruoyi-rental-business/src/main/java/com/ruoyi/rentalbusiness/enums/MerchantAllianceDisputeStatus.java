package com.ruoyi.rentalbusiness.enums;

/** 联盟纠纷状态，与 ry_rental_order_alliance_dispute.dispute_status 保持映射。 */
public enum MerchantAllianceDisputeStatus
{
    PENDING(0), EVIDENCE(1), MEDIATING(2), CONFIRMING(3), RESOLVED(4), REJECTED(5), CLOSED(6);

    private final int code;

    MerchantAllianceDisputeStatus(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static MerchantAllianceDisputeStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAllianceDisputeStatus status : values())
            {
                if (status.code == code)
                {
                    return status;
                }
            }
        }
        return null;
    }
}
