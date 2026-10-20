package com.ruoyi.merchant.model;

import java.math.BigDecimal;

/** 商户提现申请视图。收款账户只返回脱敏值。 */
public class MerchantWithdrawalVO
{
    private String withdrawalNo;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal actualAmount;
    private String withdrawalChannel;
    private String withdrawalChannelName;
    private String payeeName;
    private String payeeAccount;
    private Integer withdrawalStatus;
    private String withdrawalStatusName;
    private String reviewRemark;
    private String reviewTime;
    private String paidTime;
    private String createTime;
    private String remark;

    public String getWithdrawalNo() { return withdrawalNo; }
    public void setWithdrawalNo(String withdrawalNo) { this.withdrawalNo = withdrawalNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public String getWithdrawalChannel() { return withdrawalChannel; }
    public void setWithdrawalChannel(String withdrawalChannel) { this.withdrawalChannel = withdrawalChannel; }
    public String getWithdrawalChannelName() { return withdrawalChannelName; }
    public void setWithdrawalChannelName(String withdrawalChannelName) { this.withdrawalChannelName = withdrawalChannelName; }
    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    public String getPayeeAccount() { return payeeAccount; }
    public void setPayeeAccount(String payeeAccount) { this.payeeAccount = payeeAccount; }
    public Integer getWithdrawalStatus() { return withdrawalStatus; }
    public void setWithdrawalStatus(Integer withdrawalStatus) { this.withdrawalStatus = withdrawalStatus; }
    public String getWithdrawalStatusName() { return withdrawalStatusName; }
    public void setWithdrawalStatusName(String withdrawalStatusName) { this.withdrawalStatusName = withdrawalStatusName; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public String getReviewTime() { return reviewTime; }
    public void setReviewTime(String reviewTime) { this.reviewTime = reviewTime; }
    public String getPaidTime() { return paidTime; }
    public void setPaidTime(String paidTime) { this.paidTime = paidTime; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
