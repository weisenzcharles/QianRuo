package com.ruoyi.rentalbusiness.model;

import java.util.Date;

/** 售后处理时间线视图。 */
public class MerchantAfterSalesEventVO
{
    public Integer eventType;
    public String eventTypeLabel;
    public Integer fromStatus;
    public String fromStatusLabel;
    public Integer toStatus;
    public String toStatusLabel;
    public String content;
    public String operatorName;
    public Date createTime;
}
