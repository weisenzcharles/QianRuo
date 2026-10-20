package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台查看联盟成员共享范围的只读视图。 */
public class PlatformAllianceScopeView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Integer scopeType;
    private String scopeName;
    private String scopeCode;
    private String brand;
    private String modelName;
    private BigDecimal shareQuantityRatio;
    private Integer shareQuantityLimit;
    private Integer acceptOrderEnabled;
    private Integer shareStatus;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
    public String getScopeName() { return scopeName; }
    public void setScopeName(String scopeName) { this.scopeName = scopeName; }
    public String getScopeCode() { return scopeCode; }
    public void setScopeCode(String scopeCode) { this.scopeCode = scopeCode; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public BigDecimal getShareQuantityRatio() { return shareQuantityRatio; }
    public void setShareQuantityRatio(BigDecimal shareQuantityRatio) { this.shareQuantityRatio = shareQuantityRatio; }
    public Integer getShareQuantityLimit() { return shareQuantityLimit; }
    public void setShareQuantityLimit(Integer shareQuantityLimit) { this.shareQuantityLimit = shareQuantityLimit; }
    public Integer getAcceptOrderEnabled() { return acceptOrderEnabled; }
    public void setAcceptOrderEnabled(Integer acceptOrderEnabled) { this.acceptOrderEnabled = acceptOrderEnabled; }
    public Integer getShareStatus() { return shareStatus; }
    public void setShareStatus(Integer shareStatus) { this.shareStatus = shareStatus; }
}
