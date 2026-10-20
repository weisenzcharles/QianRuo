package com.ruoyi.system.api.model;

import java.io.Serializable;

/**
 * 平台查看商户履约、物流和风险治理汇总的只读视图。
 *
 * <p>该对象只返回可供平台治理页面展示的数量，不包含租户、商户、订单、物流或告警技术主键。</p>
 */
public class PlatformMerchantGovernanceView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long totalOrderCount;
    private long pendingOrderCount;
    private long activeOrderCount;
    private long completedOrderCount;
    private long overdueOrderCount;
    private long afterSalesCaseCount;
    private long openAfterSalesCaseCount;
    private long logisticsCount;
    private long pendingRouteCount;
    private long abnormalLogisticsCount;
    private long openInventoryAlertCount;
    private long openAllianceDisputeCount;

    public long getTotalOrderCount() { return totalOrderCount; }
    public void setTotalOrderCount(long totalOrderCount) { this.totalOrderCount = totalOrderCount; }
    public long getPendingOrderCount() { return pendingOrderCount; }
    public void setPendingOrderCount(long pendingOrderCount) { this.pendingOrderCount = pendingOrderCount; }
    public long getActiveOrderCount() { return activeOrderCount; }
    public void setActiveOrderCount(long activeOrderCount) { this.activeOrderCount = activeOrderCount; }
    public long getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(long completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public long getOverdueOrderCount() { return overdueOrderCount; }
    public void setOverdueOrderCount(long overdueOrderCount) { this.overdueOrderCount = overdueOrderCount; }
    public long getAfterSalesCaseCount() { return afterSalesCaseCount; }
    public void setAfterSalesCaseCount(long afterSalesCaseCount) { this.afterSalesCaseCount = afterSalesCaseCount; }
    public long getOpenAfterSalesCaseCount() { return openAfterSalesCaseCount; }
    public void setOpenAfterSalesCaseCount(long openAfterSalesCaseCount) { this.openAfterSalesCaseCount = openAfterSalesCaseCount; }
    public long getLogisticsCount() { return logisticsCount; }
    public void setLogisticsCount(long logisticsCount) { this.logisticsCount = logisticsCount; }
    public long getPendingRouteCount() { return pendingRouteCount; }
    public void setPendingRouteCount(long pendingRouteCount) { this.pendingRouteCount = pendingRouteCount; }
    public long getAbnormalLogisticsCount() { return abnormalLogisticsCount; }
    public void setAbnormalLogisticsCount(long abnormalLogisticsCount) { this.abnormalLogisticsCount = abnormalLogisticsCount; }
    public long getOpenInventoryAlertCount() { return openInventoryAlertCount; }
    public void setOpenInventoryAlertCount(long openInventoryAlertCount) { this.openInventoryAlertCount = openInventoryAlertCount; }
    public long getOpenAllianceDisputeCount() { return openAllianceDisputeCount; }
    public void setOpenAllianceDisputeCount(long openAllianceDisputeCount) { this.openAllianceDisputeCount = openAllianceDisputeCount; }
}
