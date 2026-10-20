package com.ruoyi.rentalbusiness.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** 商户订单字段变更记录，仅返回当前商户订单的历史修改内容。 */
public class MerchantOrderEditLogVO
{
    public Long logId;
    public String fieldName;
    @JsonIgnore
    public String beforeValue;
    @JsonIgnore
    public String afterValue;
    public String beforeDisplayValue;
    public String afterDisplayValue;
    public String changeReason;
    @JsonIgnore
    public Long operatorId;
    public String operatorName;
    public String operatorIp;
    public Date createTime;
}
