package com.ruoyi.system.api.model;

/** 商户提交信用申诉后的工单摘要。 */
public class MerchantCreditAppealView
{
    private String ticketNo;
    private String recordNo;
    private Integer ticketStatus;
    private String ticketStatusName;
    private String createTime;

    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public Integer getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(Integer ticketStatus) { this.ticketStatus = ticketStatus; }
    public String getTicketStatusName() { return ticketStatusName; }
    public void setTicketStatusName(String ticketStatusName) { this.ticketStatusName = ticketStatusName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
