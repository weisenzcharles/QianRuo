package com.ruoyi.system.model.rental;

/** 平台客服工单列表查询条件。 */
public class PlatformTicketQuery
{
    private String ticketNo;
    private Integer ticketType;
    private Integer ticketStatus;
    private Integer priority;
    private String memberKeyword;
    private String keyword;

    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
    public Integer getTicketType() { return ticketType; }
    public void setTicketType(Integer ticketType) { this.ticketType = ticketType; }
    public Integer getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(Integer ticketStatus) { this.ticketStatus = ticketStatus; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getMemberKeyword() { return memberKeyword; }
    public void setMemberKeyword(String memberKeyword) { this.memberKeyword = memberKeyword; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
