package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台售后工单关联结果，只返回商户页面需要的工单编码和状态。 */
public class PlatformAfterSalesTicketView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String ticketNo;
    private Integer ticketStatus;
    private String ticketStatusName;

    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String value) { ticketNo = value; }
    public Integer getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(Integer value) { ticketStatus = value; }
    public String getTicketStatusName() { return ticketStatusName; }
    public void setTicketStatusName(String value) { ticketStatusName = value; }
}
