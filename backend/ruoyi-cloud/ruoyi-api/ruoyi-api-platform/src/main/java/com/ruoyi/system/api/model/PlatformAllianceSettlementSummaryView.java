package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台联盟结算汇总视图。 */
public class PlatformAllianceSettlementSummaryView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long totalOrderCount;
    private long pendingOrderCount;
    private long confirmedOrderCount;
    private long disputedOrderCount;
    private long closedOrderCount;
    private BigDecimal settlementAmount = BigDecimal.ZERO;
    private BigDecimal pendingSettlementAmount = BigDecimal.ZERO;
    private BigDecimal confirmedSettlementAmount = BigDecimal.ZERO;
    private BigDecimal disputedSettlementAmount = BigDecimal.ZERO;
    private BigDecimal closedSettlementAmount = BigDecimal.ZERO;
    private BigDecimal expressAmount = BigDecimal.ZERO;
    private BigDecimal compensationAmount = BigDecimal.ZERO;

    public long getTotalOrderCount() { return totalOrderCount; }
    public void setTotalOrderCount(long value) { totalOrderCount = value; }
    public long getPendingOrderCount() { return pendingOrderCount; }
    public void setPendingOrderCount(long value) { pendingOrderCount = value; }
    public long getConfirmedOrderCount() { return confirmedOrderCount; }
    public void setConfirmedOrderCount(long value) { confirmedOrderCount = value; }
    public long getDisputedOrderCount() { return disputedOrderCount; }
    public void setDisputedOrderCount(long value) { disputedOrderCount = value; }
    public long getClosedOrderCount() { return closedOrderCount; }
    public void setClosedOrderCount(long value) { closedOrderCount = value; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal value) { settlementAmount = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getPendingSettlementAmount() { return pendingSettlementAmount; }
    public void setPendingSettlementAmount(BigDecimal value) { pendingSettlementAmount = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getConfirmedSettlementAmount() { return confirmedSettlementAmount; }
    public void setConfirmedSettlementAmount(BigDecimal value) { confirmedSettlementAmount = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getDisputedSettlementAmount() { return disputedSettlementAmount; }
    public void setDisputedSettlementAmount(BigDecimal value) { disputedSettlementAmount = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getClosedSettlementAmount() { return closedSettlementAmount; }
    public void setClosedSettlementAmount(BigDecimal value) { closedSettlementAmount = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getExpressAmount() { return expressAmount; }
    public void setExpressAmount(BigDecimal value) { expressAmount = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal value) { compensationAmount = value == null ? BigDecimal.ZERO : value; }
}
