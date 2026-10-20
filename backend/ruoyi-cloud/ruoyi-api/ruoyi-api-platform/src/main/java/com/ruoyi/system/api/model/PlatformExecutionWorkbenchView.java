package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 租赁业务服务提供的平台执行面待办汇总。 */
public class PlatformExecutionWorkbenchView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Boolean available;
    private Integer fulfillmentTaskCount = 0;
    private Integer afterSalesTaskCount = 0;
    private Integer logisticsTaskCount = 0;
    private Integer inventoryAlertTaskCount = 0;
    private List<PlatformExecutionWorkbenchTaskView> tasks = Collections.emptyList();

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public Integer getFulfillmentTaskCount() { return fulfillmentTaskCount; }
    public void setFulfillmentTaskCount(Integer fulfillmentTaskCount) { this.fulfillmentTaskCount = fulfillmentTaskCount; }
    public Integer getAfterSalesTaskCount() { return afterSalesTaskCount; }
    public void setAfterSalesTaskCount(Integer afterSalesTaskCount) { this.afterSalesTaskCount = afterSalesTaskCount; }
    public Integer getLogisticsTaskCount() { return logisticsTaskCount; }
    public void setLogisticsTaskCount(Integer logisticsTaskCount) { this.logisticsTaskCount = logisticsTaskCount; }
    public Integer getInventoryAlertTaskCount() { return inventoryAlertTaskCount; }
    public void setInventoryAlertTaskCount(Integer inventoryAlertTaskCount) { this.inventoryAlertTaskCount = inventoryAlertTaskCount; }
    public List<PlatformExecutionWorkbenchTaskView> getTasks() { return tasks; }
    public void setTasks(List<PlatformExecutionWorkbenchTaskView> tasks) { this.tasks = tasks; }
}
