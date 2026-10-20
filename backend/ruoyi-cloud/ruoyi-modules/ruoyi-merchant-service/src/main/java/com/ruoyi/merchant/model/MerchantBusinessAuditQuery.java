package com.ruoyi.merchant.model;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户业务审计列表查询条件，归属范围由服务端登录态决定。 */
public class MerchantBusinessAuditQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String businessModule;
    private String operationType;
    private String username;
    private String operatorName;
    private String description;
    private String ipAddress;
    private Integer status;

    public String getBusinessModule() { return businessModule; }
    public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    /**
     * 获取按商户用户表用户名过滤的条件。
     *
     * @return 用户名模糊查询条件
     */
    public String getUsername() { return username; }

    /**
     * 设置按商户用户表用户名过滤的条件。
     *
     * @param username 用户名模糊查询条件
     */
    public void setUsername(String username) { this.username = username; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    /**
     * 获取按业务描述过滤的条件。
     *
     * @return 业务描述模糊查询条件
     */
    public String getDescription() { return description; }

    /**
     * 设置按业务描述过滤的条件。
     *
     * @param description 业务描述模糊查询条件
     */
    public void setDescription(String description) { this.description = description; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
