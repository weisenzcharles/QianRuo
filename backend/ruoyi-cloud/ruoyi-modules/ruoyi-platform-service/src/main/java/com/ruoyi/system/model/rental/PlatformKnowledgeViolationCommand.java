package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 平台对商户知识文章执行违规处置的入参。 */
public class PlatformKnowledgeViolationCommand
{
    @NotBlank(message = "违规原因不能为空")
    @Size(max = 500, message = "违规原因不能超过500个字符")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
