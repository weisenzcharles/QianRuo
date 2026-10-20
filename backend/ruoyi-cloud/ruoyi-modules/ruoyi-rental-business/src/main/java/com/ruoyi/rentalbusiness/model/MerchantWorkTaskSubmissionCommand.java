package com.ruoyi.rentalbusiness.model;

/** 提交商户工作任务命令。提交记录不可覆盖，只能追加新的提交。 */
public class MerchantWorkTaskSubmissionCommand
{
    public String completionNote;
    public String evidenceJson;
}
