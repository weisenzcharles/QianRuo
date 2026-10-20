package com.ruoyi.merchant.model;

import java.math.BigDecimal;

/** 商户提交提现申请参数。 */
public class MerchantWithdrawalApplyCommand
{
    private BigDecimal amount;
    private MerchantWithdrawalChannel withdrawalChannel;
    private String payeeName;
    private String payeeAccount;
    private String idempotencyKey;
    private String remark;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public MerchantWithdrawalChannel getWithdrawalChannel() { return withdrawalChannel; }
    public void setWithdrawalChannel(MerchantWithdrawalChannel withdrawalChannel) { this.withdrawalChannel = withdrawalChannel; }
    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    public String getPayeeAccount() { return payeeAccount; }
    public void setPayeeAccount(String payeeAccount) { this.payeeAccount = payeeAccount; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
