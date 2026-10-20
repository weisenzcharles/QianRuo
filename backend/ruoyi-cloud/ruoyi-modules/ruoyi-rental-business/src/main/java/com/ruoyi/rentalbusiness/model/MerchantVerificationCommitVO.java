package com.ruoyi.rentalbusiness.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 极速核销台批量提交结果。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantVerificationCommitVO
{
    public boolean success;
    public String modeLabel;
    public String orderNo;
    public int successCount;
    public int skippedCount;
    public String message;
}
