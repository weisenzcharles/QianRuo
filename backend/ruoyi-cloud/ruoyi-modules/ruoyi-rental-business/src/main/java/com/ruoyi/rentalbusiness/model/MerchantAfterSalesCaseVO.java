package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/** 商户售后单视图，只返回用户需要的售后单号、订单编码和业务信息。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantAfterSalesCaseVO
{
    @JsonIgnore
    public Long caseId;
    @JsonIgnore
    public Long orderId;
    public String caseNo;
    public String orderNo;
    public Integer caseType;
    public String caseTypeLabel;
    public Integer caseStatus;
    public String caseStatusLabel;
    public String problemDesc;
    public String lostItemsJson;
    public BigDecimal compensationAmount;
    public String platformTicketNo;
    public Integer customerContacted;
    public Date createTime;
    public Date finishTime;
    public String remark;
}
