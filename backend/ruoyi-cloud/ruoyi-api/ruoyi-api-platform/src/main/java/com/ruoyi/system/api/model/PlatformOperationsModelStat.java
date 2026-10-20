package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台商品热度聚合指标，供平台服务内部组装使用。 */
public class PlatformOperationsModelStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String modelName;
    private long orderCount;
    private long quantity;
    private long completedOrderCount;
    private long overdueOrderCount;

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public long getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(long completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public long getOverdueOrderCount() { return overdueOrderCount; }
    public void setOverdueOrderCount(long overdueOrderCount) { this.overdueOrderCount = overdueOrderCount; }
}
