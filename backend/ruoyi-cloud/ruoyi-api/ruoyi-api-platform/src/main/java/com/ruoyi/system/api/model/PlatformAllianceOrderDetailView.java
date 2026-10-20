package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台查看的联盟订单详情及其事件、结算记录。 */
public class PlatformAllianceOrderDetailView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private PlatformAllianceOrderView order;
    private List<PlatformAllianceOrderEventView> events = Collections.emptyList();
    private List<PlatformAllianceSettlementView> settlements = Collections.emptyList();

    public PlatformAllianceOrderView getOrder() { return order; }
    public void setOrder(PlatformAllianceOrderView value) { order = value; }
    public List<PlatformAllianceOrderEventView> getEvents() { return events; }
    public void setEvents(List<PlatformAllianceOrderEventView> value) { events = value; }
    public List<PlatformAllianceSettlementView> getSettlements() { return settlements; }
    public void setSettlements(List<PlatformAllianceSettlementView> value) { settlements = value; }
}
