package com.ruoyi.merchant.domain;

/**
 * 商户业务审计日志持久化对象。
 *
 * <p>该对象只承载业务审计字段，最终映射到租赁域统一的
 * {@code ry_rental.ry_rental_audit_log} 事实表，不包含请求方式、请求地址和控制器方法名。
 * 所有租户与商户归属字段由商户服务内部接口或当前登录上下文补齐。</p>
 */
public class MerchantBusinessAuditRecord
{
    private Long auditId;
    private Long tenantId;
    private Long memberId;
    private Long operatorUserId;
    private String operatorName;
    private String businessModule;
    private String operationType;
    private String targetType;
    private String targetId;
    private String targetName;
    private String description;
    private String beforeContent;
    private String afterContent;
    private String ipAddress;
    private Integer status;
    private String errorMessage;
    private String traceId;
    private Integer isDelete;

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getBusinessModule() { return businessModule; }
    public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBeforeContent() { return beforeContent; }
    public void setBeforeContent(String beforeContent) { this.beforeContent = beforeContent; }
    public String getAfterContent() { return afterContent; }
    public void setAfterContent(String afterContent) { this.afterContent = afterContent; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
}
