package com.ruoyi.rentalbusiness.enums;

/** 联盟纠纷类型，与 ry_rental_order_alliance_dispute.dispute_type 保持映射。 */
public enum MerchantAllianceDisputeType
{
    GENERAL(0), DAMAGE(1), LOSS(2), DELAYED_FULFILLMENT(3), FEE(4), OTHER(9);

    private final int code;

    MerchantAllianceDisputeType(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static MerchantAllianceDisputeType fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAllianceDisputeType type : values())
            {
                if (type.code == code)
                {
                    return type;
                }
            }
        }
        return null;
    }
}
