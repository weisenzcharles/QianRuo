package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台售后案件详情及事件、证据。 */
public class PlatformAfterSalesDetailView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PlatformAfterSalesView afterSales;
    private List<PlatformAfterSalesEventView> events = Collections.emptyList();
    private List<PlatformAfterSalesEvidenceView> evidence = Collections.emptyList();

    public PlatformAfterSalesView getAfterSales() { return afterSales; }
    public void setAfterSales(PlatformAfterSalesView value) { afterSales = value; }
    public List<PlatformAfterSalesEventView> getEvents() { return events; }
    public void setEvents(List<PlatformAfterSalesEventView> value) { events = value; }
    public List<PlatformAfterSalesEvidenceView> getEvidence() { return evidence; }
    public void setEvidence(List<PlatformAfterSalesEvidenceView> value) { evidence = value; }
}
