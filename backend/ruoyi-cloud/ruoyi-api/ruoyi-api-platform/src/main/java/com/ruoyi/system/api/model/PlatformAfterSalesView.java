package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/** 平台售后案件视图。关联编号仅供后端操作使用，页面只展示业务编码和名称。 */
public class PlatformAfterSalesView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long caseId;
    private Long tenantId;
    private Long memberId;
    private Long orderId;
    private String caseNo;
    private String orderNo;
    private String merchantName;
    private String merchantCode;
    private Integer caseType;
    private String caseTypeName;
    private Integer caseStatus;
    private String caseStatusName;
    private String problemDesc;
    private String lostItemsJson;
    private BigDecimal compensationAmount;
    private String platformTicketNo;
    private Integer customerContacted;
    private Date createTime;
    private Date finishTime;
    private String remark;

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long value) { caseId = value; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long value) { tenantId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String value) { caseNo = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String value) { merchantName = value; }
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String value) { merchantCode = value; }
    public Integer getCaseType() { return caseType; }
    public void setCaseType(Integer value) { caseType = value; }
    public String getCaseTypeName() { return caseTypeName; }
    public void setCaseTypeName(String value) { caseTypeName = value; }
    public Integer getCaseStatus() { return caseStatus; }
    public void setCaseStatus(Integer value) { caseStatus = value; }
    public String getCaseStatusName() { return caseStatusName; }
    public void setCaseStatusName(String value) { caseStatusName = value; }
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String value) { problemDesc = value; }
    public String getLostItemsJson() { return lostItemsJson; }
    public void setLostItemsJson(String value) { lostItemsJson = value; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal value) { compensationAmount = value; }
    public String getPlatformTicketNo() { return platformTicketNo; }
    public void setPlatformTicketNo(String value) { platformTicketNo = value; }
    public Integer getCustomerContacted() { return customerContacted; }
    public void setCustomerContacted(Integer value) { customerContacted = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date value) { finishTime = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
