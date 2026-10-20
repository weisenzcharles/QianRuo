package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemotePlatformGovernanceService;

/** 平台统一调度服务中的回收站过期清理任务入口。 */
@Component("platformRecycleCleanupJob")
public class PlatformRecycleCleanupJob
{
    private static final Logger log = LoggerFactory.getLogger(PlatformRecycleCleanupJob.class);
    private final RemotePlatformGovernanceService governanceService;

    public PlatformRecycleCleanupJob(RemotePlatformGovernanceService governanceService)
    {
        this.governanceService = governanceService;
    }

    /** 清理超过 30 天且未恢复的回收记录，并让调度平台记录远程失败。 */
    public void cleanup()
    {
        log.info("平台回收站过期清理任务开始");
        R<Integer> response = governanceService.cleanupExpiredRecycleRecords(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("平台回收站过期清理失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("平台回收站过期清理任务完成，清理记录数={}", response.getData());
    }
}
