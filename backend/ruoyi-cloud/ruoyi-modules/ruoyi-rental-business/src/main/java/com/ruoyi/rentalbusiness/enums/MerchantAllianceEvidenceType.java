package com.ruoyi.rentalbusiness.enums;

/** 联盟纠纷证据类型，与 ry_rental_order_alliance_dispute_evidence.evidence_type 保持映射。 */
public enum MerchantAllianceEvidenceType
{
    IMAGE(1), VIDEO(2), FILE(3), TEXT(4);

    private final int code;

    MerchantAllianceEvidenceType(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static MerchantAllianceEvidenceType fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantAllianceEvidenceType type : values())
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
