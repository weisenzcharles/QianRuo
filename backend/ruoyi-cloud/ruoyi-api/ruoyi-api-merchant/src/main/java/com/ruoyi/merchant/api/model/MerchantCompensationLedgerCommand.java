package com.ruoyi.merchant.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 物流赔付入账命令。由租赁业务服务在赔付审核通过后通过内部契约提交。 */
public class MerchantCompensationLedgerCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private Long memberId;
    private Long claimId;
    private String claimNo;
    private BigDecimal amount;
    private String idempotencyKey;
    private Long operatorId;
    private String remark;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }
    public String getClaimNo() { return claimNo; }
    public void setClaimNo(String claimNo) { this.claimNo = claimNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
