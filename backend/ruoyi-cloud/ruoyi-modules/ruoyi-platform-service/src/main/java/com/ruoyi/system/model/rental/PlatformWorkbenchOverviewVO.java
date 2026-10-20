package com.ruoyi.system.model.rental;

import java.math.BigDecimal;
import java.util.List;

/** 平台通用工作台汇总视图。 */
public class PlatformWorkbenchOverviewVO
{
    private String generatedAt;
    private Integer totalTaskCount;
    private Integer riskTaskCount;
    private Integer ticketTaskCount;
    private Integer withdrawalTaskCount;
    private Integer invoiceTaskCount;
    private Integer marketReportTaskCount;
    private Integer fulfillmentTaskCount;
    private Integer afterSalesTaskCount;
    private Integer logisticsTaskCount;
    private Integer inventoryAlertTaskCount;
    private Boolean executionAvailable;
    private BigDecimal pendingWithdrawalAmount;
    private List<PlatformWorkbenchTaskVO> tasks;

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    public Integer getTotalTaskCount() { return totalTaskCount; }
    public void setTotalTaskCount(Integer totalTaskCount) { this.totalTaskCount = totalTaskCount; }
    public Integer getRiskTaskCount() { return riskTaskCount; }
    public void setRiskTaskCount(Integer riskTaskCount) { this.riskTaskCount = riskTaskCount; }
    public Integer getTicketTaskCount() { return ticketTaskCount; }
    public void setTicketTaskCount(Integer ticketTaskCount) { this.ticketTaskCount = ticketTaskCount; }
    public Integer getWithdrawalTaskCount() { return withdrawalTaskCount; }
    public void setWithdrawalTaskCount(Integer withdrawalTaskCount) { this.withdrawalTaskCount = withdrawalTaskCount; }
    public Integer getInvoiceTaskCount() { return invoiceTaskCount; }
    public void setInvoiceTaskCount(Integer invoiceTaskCount) { this.invoiceTaskCount = invoiceTaskCount; }
    public Integer getMarketReportTaskCount() { return marketReportTaskCount; }
    public void setMarketReportTaskCount(Integer marketReportTaskCount) { this.marketReportTaskCount = marketReportTaskCount; }
    public Integer getFulfillmentTaskCount() { return fulfillmentTaskCount; }
    public void setFulfillmentTaskCount(Integer fulfillmentTaskCount) { this.fulfillmentTaskCount = fulfillmentTaskCount; }
    public Integer getAfterSalesTaskCount() { return afterSalesTaskCount; }
    public void setAfterSalesTaskCount(Integer afterSalesTaskCount) { this.afterSalesTaskCount = afterSalesTaskCount; }
    public Integer getLogisticsTaskCount() { return logisticsTaskCount; }
    public void setLogisticsTaskCount(Integer logisticsTaskCount) { this.logisticsTaskCount = logisticsTaskCount; }
    public Integer getInventoryAlertTaskCount() { return inventoryAlertTaskCount; }
    public void setInventoryAlertTaskCount(Integer inventoryAlertTaskCount) { this.inventoryAlertTaskCount = inventoryAlertTaskCount; }
    public Boolean getExecutionAvailable() { return executionAvailable; }
    public void setExecutionAvailable(Boolean executionAvailable) { this.executionAvailable = executionAvailable; }
    public BigDecimal getPendingWithdrawalAmount() { return pendingWithdrawalAmount; }
    public void setPendingWithdrawalAmount(BigDecimal pendingWithdrawalAmount) { this.pendingWithdrawalAmount = pendingWithdrawalAmount; }
    public List<PlatformWorkbenchTaskVO> getTasks() { return tasks; }
    public void setTasks(List<PlatformWorkbenchTaskVO> tasks) { this.tasks = tasks; }
}
