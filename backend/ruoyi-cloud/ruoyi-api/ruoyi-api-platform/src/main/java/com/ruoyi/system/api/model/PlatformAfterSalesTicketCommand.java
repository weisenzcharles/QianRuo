package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商户售后申请平台介入的内部命令。
 *
 * <p>命令只在租赁业务服务与平台服务之间传递，平台工单以售后唯一编号做幂等引用。</p>
 */
public class PlatformAfterSalesTicketCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Long caseId;
    private Long orderId;
    private String caseNo;
    private String orderNo;
    private String caseTypeName;
    private String problemDesc;
    private BigDecimal compensationAmount;
    private String remark;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long value) { caseId = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String value) { caseNo = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getCaseTypeName() { return caseTypeName; }
    public void setCaseTypeName(String value) { caseTypeName = value; }
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String value) { problemDesc = value; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal value) { compensationAmount = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
