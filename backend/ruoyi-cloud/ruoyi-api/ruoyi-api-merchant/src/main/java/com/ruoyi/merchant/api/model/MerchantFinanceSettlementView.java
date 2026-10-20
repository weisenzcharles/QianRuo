package com.ruoyi.merchant.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 租赁业务服务返回给商户财务中心的联盟结算只读视图。 */
public class MerchantFinanceSettlementView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private String sourceOrderNo;
    private String childOrderNo;
    private String roleName;
    private BigDecimal settlementAmount;
    private BigDecimal expressAmount;
    private BigDecimal compensationAmount;
    private Integer settlementStatus;
    private String settlementStatusName;
    private String createTime;
    private String updateTime;
    private String remark;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String sourceOrderNo) { this.sourceOrderNo = sourceOrderNo; }
    public String getChildOrderNo() { return childOrderNo; }
    public void setChildOrderNo(String childOrderNo) { this.childOrderNo = childOrderNo; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal settlementAmount) { this.settlementAmount = settlementAmount; }
    public BigDecimal getExpressAmount() { return expressAmount; }
    public void setExpressAmount(BigDecimal expressAmount) { this.expressAmount = expressAmount; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal compensationAmount) { this.compensationAmount = compensationAmount; }
    public Integer getSettlementStatus() { return settlementStatus; }
    public void setSettlementStatus(Integer settlementStatus) { this.settlementStatus = settlementStatus; }
    public String getSettlementStatusName() { return settlementStatusName; }
    public void setSettlementStatusName(String settlementStatusName) { this.settlementStatusName = settlementStatusName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
