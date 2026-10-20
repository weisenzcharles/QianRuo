package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemotePlatformGovernanceService;

/** 平台统一调度服务中的平台文件存储容量采集任务入口。 */
@Component("platformCapacityRefreshJob")
public class PlatformCapacityRefreshJob
{
    private static final Logger log = LoggerFactory.getLogger(PlatformCapacityRefreshJob.class);

    private final RemotePlatformGovernanceService governanceService;

    public PlatformCapacityRefreshJob(RemotePlatformGovernanceService governanceService)
    {
        this.governanceService = governanceService;
    }

    /** 触发平台服务采集配置路径容量，失败时抛出异常让调度日志可追踪。 */
    public void refresh()
    {
        log.info("平台文件存储容量采集任务开始");
        R<Integer> response = governanceService.collectPlatformCapacity(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("平台文件存储容量采集失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("平台文件存储容量采集任务完成，采集范围数={}", response.getData());
    }
}
