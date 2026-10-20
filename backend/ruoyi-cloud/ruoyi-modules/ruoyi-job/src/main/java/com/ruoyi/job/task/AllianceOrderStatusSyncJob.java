package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 统一调度服务中的联盟订单履约状态同步任务入口。 */
@Component("allianceOrderStatusSyncJob")
public class AllianceOrderStatusSyncJob
{
    private static final Logger log = LoggerFactory.getLogger(AllianceOrderStatusSyncJob.class);

    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public AllianceOrderStatusSyncJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 同步子订单状态并释放已结束联盟订单的接单名额。 */
    public void synchronize()
    {
        log.info("联盟订单履约状态同步任务开始");
        R<Integer> response = remoteRentalBusinessService.synchronizeAllianceOrderStatuses(SecurityConstants.INNER);
        if (response == null)
        {
            throw new IllegalStateException("rental-business 联盟状态同步接口返回空响应");
        }
        if (!R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 联盟状态同步失败：" + response.getMsg());
        }
        log.info("联盟订单履约状态同步任务完成：changed={}", response.getData());
    }
}
