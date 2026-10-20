package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 统一调度服务中的订单履约状态刷新任务入口。 */
@Component("orderLifecycleRefreshJob")
public class OrderLifecycleRefreshJob
{
    private static final Logger log = LoggerFactory.getLogger(OrderLifecycleRefreshJob.class);
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public OrderLifecycleRefreshJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 调用 rental-business 刷新发货逾期、租用中和归还逾期状态。 */
    public void refresh()
    {
        log.info("订单履约状态刷新任务开始");
        R<Integer> response = remoteRentalBusinessService.refreshOrderLifecycleStatuses(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 订单履约状态刷新失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("订单履约状态刷新任务完成，推进订单数={}", response.getData());
    }
}
