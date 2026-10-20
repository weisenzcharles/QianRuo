package com.ruoyi.merchant.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 物流赔付入账结果。 */
public class MerchantCompensationLedgerResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String ledgerNo;
    private BigDecimal balanceAfter;
    private boolean alreadySettled;

    public String getLedgerNo() { return ledgerNo; }
    public void setLedgerNo(String ledgerNo) { this.ledgerNo = ledgerNo; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public boolean isAlreadySettled() { return alreadySettled; }
    public void setAlreadySettled(boolean alreadySettled) { this.alreadySettled = alreadySettled; }
}
