package com.ruoyi.merchant.model;

/** 本地支付通知入账结果，供通知处理完成订单状态同步。 */
public class MerchantPaymentSettlementResult
{
    private MerchantPaymentRecord payment;
    private boolean alreadyPaid;

    public MerchantPaymentRecord getPayment() { return payment; }
    public void setPayment(MerchantPaymentRecord payment) { this.payment = payment; }
    public boolean isAlreadyPaid() { return alreadyPaid; }
    public void setAlreadyPaid(boolean alreadyPaid) { this.alreadyPaid = alreadyPaid; }
}
