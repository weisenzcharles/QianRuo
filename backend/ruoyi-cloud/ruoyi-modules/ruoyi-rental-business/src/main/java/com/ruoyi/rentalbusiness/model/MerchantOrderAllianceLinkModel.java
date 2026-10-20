package com.ruoyi.rentalbusiness.model;

/** 当前商户订单与租商联盟原订单之间的内部关联投影。 */
public class MerchantOrderAllianceLinkModel
{
    public Long allianceOrderId;
    public Long sourceMemberId;
    public Long targetMemberId;
    public Long sourceOrderId;
    public Long childOrderId;
}
