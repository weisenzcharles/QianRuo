package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 统一调度服务中的未签收物流路线刷新任务入口。 */
@Component("logisticsRouteRefreshJob")
public class LogisticsRouteRefreshJob
{
    private static final Logger log = LoggerFactory.getLogger(LogisticsRouteRefreshJob.class);

    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public LogisticsRouteRefreshJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 刷新达到间隔且尚未签收的物流路线，并让调度平台准确记录远程失败。 */
    public void refresh()
    {
        log.info("未签收物流路线刷新任务开始");
        R<Integer> response = remoteRentalBusinessService.refreshLogisticsRoutes(SecurityConstants.INNER);
        if (response == null)
        {
            throw new IllegalStateException("rental-business 物流路线刷新接口返回空响应");
        }
        if (!R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 物流路线刷新失败：" + response.getMsg());
        }
        log.info("未签收物流路线刷新任务完成：refreshed={}", response.getData());
    }
}
