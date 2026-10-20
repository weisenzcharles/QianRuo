package com.ruoyi.merchant.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;

/**
 * 商户业务审计日志返回对象。
 *
 * <p>列表和详情共用该对象；前端列表只展示摘要字段，详情接口才返回前后快照。
 * 归属唯一编号不返回给浏览器，避免把租户隔离实现暴露给商户用户。</p>
 */
public class MerchantBusinessAuditVO
{
    @Excel(name = "审计编号", cellType = ColumnType.NUMERIC)
    private Long auditId;
    @Excel(name = "操作人")
    private String operatorName;
    @Excel(name = "用户名")
    private String username;
    @Excel(name = "业务模块")
    private String businessModule;
    @Excel(name = "操作类型")
    private String operationType;
    @Excel(name = "描述")
    private String description;
    @Excel(name = "IP地址")
    private String ipAddress;
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;
    @JsonIgnore
    private Long operatorUserId;
    @JsonIgnore
    private String targetType;
    @JsonIgnore
    private String targetId;
    private String targetName;
    private String beforeContent;
    private String afterContent;
    @JsonIgnore
    private String legacyBeforeContent;
    @JsonIgnore
    private String legacyAfterContent;
    private String errorMessage;
    @JsonIgnore
    private String traceId;

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getBusinessModule() { return businessModule; }
    public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getOperationTime() { return operationTime; }
    public void setOperationTime(Date operationTime) { this.operationTime = operationTime; }
    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
    /**
     * 获取与商户用户表关联解析后的登录用户名。
     *
     * @return 当前日志对应的商户用户名；用户已软删除时由日志快照名称兜底
     */
    public String getUsername() { return username; }

    /**
     * 设置与商户用户表关联解析后的登录用户名。
     *
     * @param username 当前日志对应的商户用户名
     */
    public void setUsername(String username) { this.username = username; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getBeforeContent() { return beforeContent; }
    public void setBeforeContent(String beforeContent) { this.beforeContent = beforeContent; }
    public String getAfterContent() { return afterContent; }
    public void setAfterContent(String afterContent) { this.afterContent = afterContent; }
    @JsonIgnore
    public String getLegacyBeforeContent() { return legacyBeforeContent; }
    public void setLegacyBeforeContent(String legacyBeforeContent) { this.legacyBeforeContent = legacyBeforeContent; }
    @JsonIgnore
    public String getLegacyAfterContent() { return legacyAfterContent; }
    public void setLegacyAfterContent(String legacyAfterContent) { this.legacyAfterContent = legacyAfterContent; }
    public void clearLegacyContent()
    {
        legacyBeforeContent = null;
        legacyAfterContent = null;
    }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
