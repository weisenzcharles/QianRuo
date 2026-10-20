package com.ruoyi.system.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 商户提交信用流水申诉的入参。 */
public class MerchantCreditAppealCommand
{
    @NotBlank(message = "信用流水编码不能为空")
    @Size(max = 48, message = "信用流水编码不能超过48个字符")
    private String recordNo;

    @NotBlank(message = "申诉理由不能为空")
    @Size(max = 1000, message = "申诉理由不能超过1000个字符")
    private String reason;

    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
