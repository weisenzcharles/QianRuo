package com.ruoyi.merchant.api.model;

import java.math.BigDecimal;

/** 商户保证金账户内部操作命令。 */
public class MerchantDepositOperationCommand
{
    private Long tenantId;
    private Long allianceId;
    private Long memberId;
    private BigDecimal amount;
    private String refType;
    private Long refId;
    private String idempotencyKey;
    private Long operatorId;
    private String remark;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getAllianceId() { return allianceId; }
    public void setAllianceId(Long allianceId) { this.allianceId = allianceId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
