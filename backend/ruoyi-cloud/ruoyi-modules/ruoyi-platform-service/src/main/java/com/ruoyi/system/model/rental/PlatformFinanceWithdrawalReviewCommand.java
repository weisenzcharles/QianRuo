package com.ruoyi.system.model.rental;

/** 平台审核提现参数。approved 为 true 表示完成出款，false 表示驳回并解冻。 */
public class PlatformFinanceWithdrawalReviewCommand
{
    private Boolean approved;
    private String reviewRemark;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
}
