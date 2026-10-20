package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户提交物流赔付申请命令。物流唯一编号通过路径传入。 */
public class MerchantLogisticsClaimCreateCommand
{
    public Integer claimType;
    public BigDecimal expectedAmount;
    public String evidenceJson;
    public String remark;
}
