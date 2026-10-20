package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 独立创建订单售后单的命令。 */
public class MerchantAfterSalesCreateCommand
{
    public Integer caseType;
    public String problemDesc;
    public String lostItemsJson;
    public BigDecimal compensationAmount;
    public Integer customerContacted;
    public String remark;
}
