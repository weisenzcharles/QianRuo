package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemotePlatformGovernanceService;

/** 平台统一调度服务中的运维告警同步任务入口。 */
@Component("platformAlertSyncJob")
public class PlatformAlertSyncJob
{
    private static final Logger log = LoggerFactory.getLogger(PlatformAlertSyncJob.class);

    private final RemotePlatformGovernanceService governanceService;

    public PlatformAlertSyncJob(RemotePlatformGovernanceService governanceService)
    {
        this.governanceService = governanceService;
    }

    /** 同步最近 24 小时调度失败记录，并让调度日志记录平台服务调用失败。 */
    public void sync()
    {
        log.info("平台运维告警同步任务开始");
        R<Integer> response = governanceService.synchronizeScheduledTaskAlerts(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("平台运维告警同步失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("平台运维告警同步任务完成，发现失败来源数={}", response.getData());
    }
}
