package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 平台统一调度服务中的库存告警刷新任务入口。 */
@Component("inventoryAlertRefreshJob")
public class InventoryAlertRefreshJob
{
    private static final Logger log = LoggerFactory.getLogger(InventoryAlertRefreshJob.class);
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public InventoryAlertRefreshJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 调用 rental-business 刷新所有商户库存告警并让调度日志记录远程失败。 */
    public void refresh()
    {
        log.info("库存告警刷新任务开始");
        R<Integer> response = remoteRentalBusinessService.refreshInventoryAlerts(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 库存告警刷新失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("库存告警刷新任务完成，恢复或新增事件数={}", response.getData());
    }
}
