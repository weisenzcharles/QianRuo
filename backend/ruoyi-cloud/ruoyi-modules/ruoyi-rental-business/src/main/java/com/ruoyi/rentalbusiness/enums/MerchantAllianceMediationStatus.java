package com.ruoyi.rentalbusiness.enums;

/** 联盟纠纷调解状态，与 ry_rental_order_alliance_mediation.mediation_status 保持映射。 */
public enum MerchantAllianceMediationStatus
{
    PENDING_CONFIRM(1), ACCEPTED(2), INITIATOR_REJECTED(3), RESPONDENT_REJECTED(4), WITHDRAWN(5);

    private final int code;

    MerchantAllianceMediationStatus(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
