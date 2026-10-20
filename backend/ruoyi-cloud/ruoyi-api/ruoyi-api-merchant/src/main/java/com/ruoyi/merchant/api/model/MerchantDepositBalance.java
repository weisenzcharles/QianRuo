package com.ruoyi.merchant.api.model;

import java.math.BigDecimal;

/** 保证金账户余额。 */
public class MerchantDepositBalance
{
    private Long accountId;
    private BigDecimal availableAmount;
    private BigDecimal frozenAmount;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getAvailableAmount() { return availableAmount; }
    public void setAvailableAmount(BigDecimal availableAmount) { this.availableAmount = availableAmount; }
    public BigDecimal getFrozenAmount() { return frozenAmount; }
    public void setFrozenAmount(BigDecimal frozenAmount) { this.frozenAmount = frozenAmount; }
}
