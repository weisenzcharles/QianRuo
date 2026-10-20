package com.ruoyi.merchant.api.model;

import java.io.Serializable;

/**
 * 商户业务审计写入命令。
 *
 * <p>该命令只描述业务变更，最终写入租赁域统一的 {@code ry_rental_audit_log} 事实表，
 * 不携带请求方式、请求地址和方法名等请求级字段。
 * 请求级审计由平台的 {@code sys_oper_log} 统一保存，商户审计只保存业务模块、
 * 动作、对象以及变更前后的业务快照。</p>
 */
public class MerchantBusinessAuditCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

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
    /** 不含技术编号的变更前展示快照。 */
    private String beforeDisplayContent;
    /** 不含技术编号的变更后展示快照。 */
    private String afterDisplayContent;
    private String ipAddress;
    private Integer status;
    private String errorMessage;
    private String traceId;

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
    public String getBeforeDisplayContent() { return beforeDisplayContent; }
    public void setBeforeDisplayContent(String beforeDisplayContent) { this.beforeDisplayContent = beforeDisplayContent; }
    public String getAfterDisplayContent() { return afterDisplayContent; }
    public void setAfterDisplayContent(String afterDisplayContent) { this.afterDisplayContent = afterDisplayContent; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
