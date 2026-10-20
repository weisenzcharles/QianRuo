package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 订单最近一次归还验机结果。
 *
 * <p>该对象只返回验机业务字段，数据库唯一编号和内部关联字段不暴露给商户端。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantOrderReturnInspectionVO
{
    public Integer result;
    public String resultLabel;
    public Integer caseType;
    public String caseTypeLabel;
    public String problemDesc;
    public String lostItemsJson;
    public BigDecimal compensationAmount;
    public Integer customerContacted;
    public String remark;
    public Date inspectedTime;
}
