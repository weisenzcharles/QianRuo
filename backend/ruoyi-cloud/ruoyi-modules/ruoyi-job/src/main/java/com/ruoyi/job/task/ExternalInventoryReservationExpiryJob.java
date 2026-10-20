package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 平台统一调度服务中的外部库存预占过期释放任务入口。 */
@Component("externalInventoryReservationExpiryJob")
public class ExternalInventoryReservationExpiryJob
{
    private static final Logger log = LoggerFactory.getLogger(ExternalInventoryReservationExpiryJob.class);
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public ExternalInventoryReservationExpiryJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 调用 rental-business 释放到期预占，并让调度日志记录远程失败。 */
    public void expire()
    {
        log.info("外部库存预占过期释放任务开始");
        R<Integer> response = remoteRentalBusinessService.expireExternalInventoryReservations(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 外部库存预占过期释放失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("外部库存预占过期释放任务完成，释放预占数={}", response.getData());
    }
}
