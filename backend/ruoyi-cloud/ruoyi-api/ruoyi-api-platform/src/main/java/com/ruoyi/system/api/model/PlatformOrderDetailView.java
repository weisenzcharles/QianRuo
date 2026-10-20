package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台订单详情及订单内部处理记录。 */
public class PlatformOrderDetailView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PlatformOrderView order;
    private List<PlatformOrderItemView> items = Collections.emptyList();
    private List<PlatformLogisticsDetailView> logistics = Collections.emptyList();
    private PlatformOrderReturnInspectionView returnInspection;
    private List<PlatformAfterSalesDetailView> afterSales = Collections.emptyList();
    private List<PlatformOrderFulfillmentEventView> fulfillmentEvents = Collections.emptyList();
    private List<PlatformOrderNoteView> notes = Collections.emptyList();
    private List<PlatformOrderEditLogView> editLogs = Collections.emptyList();

    public PlatformOrderView getOrder() { return order; }
    public void setOrder(PlatformOrderView order) { this.order = order; }
    public List<PlatformOrderItemView> getItems() { return items; }
    public void setItems(List<PlatformOrderItemView> items) { this.items = items; }
    public List<PlatformLogisticsDetailView> getLogistics() { return logistics; }
    public void setLogistics(List<PlatformLogisticsDetailView> logistics) { this.logistics = logistics; }
    public PlatformOrderReturnInspectionView getReturnInspection() { return returnInspection; }
    public void setReturnInspection(PlatformOrderReturnInspectionView value) { returnInspection = value; }
    public List<PlatformAfterSalesDetailView> getAfterSales() { return afterSales; }
    public void setAfterSales(List<PlatformAfterSalesDetailView> afterSales) { this.afterSales = afterSales; }
    public List<PlatformOrderFulfillmentEventView> getFulfillmentEvents() { return fulfillmentEvents; }
    public void setFulfillmentEvents(List<PlatformOrderFulfillmentEventView> fulfillmentEvents) { this.fulfillmentEvents = fulfillmentEvents; }
    public List<PlatformOrderNoteView> getNotes() { return notes; }
    public void setNotes(List<PlatformOrderNoteView> notes) { this.notes = notes; }
    public List<PlatformOrderEditLogView> getEditLogs() { return editLogs; }
    public void setEditLogs(List<PlatformOrderEditLogView> editLogs) { this.editLogs = editLogs; }
}
