package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 订单归还验机命令，正常验机完成订单，异常验机创建售后单。 */
public class MerchantOrderReturnInspectionCommand
{
    public Integer result;
    public Integer caseType;
    public String problemDesc;
    public String lostItemsJson;
    public BigDecimal compensationAmount;
    public Integer customerContacted;
    public String remark;
}
