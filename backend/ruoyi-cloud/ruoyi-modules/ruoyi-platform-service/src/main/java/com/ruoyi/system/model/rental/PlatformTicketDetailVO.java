package com.ruoyi.system.model.rental;

import java.util.List;

/** 平台客服工单详情及追加时间线。 */
public class PlatformTicketDetailVO
{
    private PlatformTicketVO ticket;
    private List<PlatformTicketAssignmentVO> assignments;
    private List<PlatformTicketEventVO> events;

    public PlatformTicketVO getTicket() { return ticket; }
    public void setTicket(PlatformTicketVO ticket) { this.ticket = ticket; }
    public List<PlatformTicketAssignmentVO> getAssignments() { return assignments; }
    public void setAssignments(List<PlatformTicketAssignmentVO> assignments) { this.assignments = assignments; }
    public List<PlatformTicketEventVO> getEvents() { return events; }
    public void setEvents(List<PlatformTicketEventVO> events) { this.events = events; }
}
