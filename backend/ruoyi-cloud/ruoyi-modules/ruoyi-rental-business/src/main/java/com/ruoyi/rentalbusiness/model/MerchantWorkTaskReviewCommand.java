package com.ruoyi.rentalbusiness.model;

import com.ruoyi.rentalbusiness.enums.MerchantWorkReviewResult;

/** 质检商户工作任务命令。评分范围固定为0至100分。 */
public class MerchantWorkTaskReviewCommand
{
    public Integer score;
    public MerchantWorkReviewResult reviewResult;
    public String reviewComment;
}
