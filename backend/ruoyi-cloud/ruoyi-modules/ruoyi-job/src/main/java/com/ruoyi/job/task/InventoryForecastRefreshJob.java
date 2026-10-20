package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 平台统一调度服务中的库存未来预测刷新任务入口。 */
@Component("inventoryForecastRefreshJob")
public class InventoryForecastRefreshJob
{
    private static final Logger log = LoggerFactory.getLogger(InventoryForecastRefreshJob.class);
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public InventoryForecastRefreshJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 调用 rental-business 独立重算未来三十天预测并让调度日志记录远程失败。 */
    public void refresh()
    {
        log.info("库存未来预测刷新任务开始");
        R<Integer> response = remoteRentalBusinessService.refreshInventoryForecast(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 库存预测刷新失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("库存未来预测刷新任务完成，写入快照数={}", response.getData());
    }
}
