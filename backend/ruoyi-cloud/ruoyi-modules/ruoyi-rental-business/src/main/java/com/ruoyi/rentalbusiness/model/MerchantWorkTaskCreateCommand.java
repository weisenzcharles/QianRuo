package com.ruoyi.rentalbusiness.model;

import java.time.LocalDateTime;
import com.ruoyi.rentalbusiness.enums.MerchantWorkTaskSourceType;
import com.ruoyi.rentalbusiness.enums.MerchantWorkTaskType;

/** 创建商户工作任务命令。 */
public class MerchantWorkTaskCreateCommand
{
    public String title;
    public String content;
    public MerchantWorkTaskSourceType sourceType;
    public MerchantWorkTaskType taskType;
    public Long warehouseId;
    public Long assigneeUserId;
    public LocalDateTime dueTime;
    public String remark;
}
