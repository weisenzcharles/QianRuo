package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 定期释放调解期限届满纠纷的冻结保证金。 */
@Component("allianceDisputeExpiryJob")
public class AllianceDisputeExpiryJob
{
    private static final Logger log = LoggerFactory.getLogger(AllianceDisputeExpiryJob.class);

    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public AllianceDisputeExpiryJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 扫描到期纠纷并执行幂等解冻。 */
    public void releaseExpired()
    {
        log.info("联盟纠纷到期解冻任务开始");
        R<Integer> response = remoteRentalBusinessService.releaseExpiredAllianceDisputes(SecurityConstants.INNER);
        if (response == null)
        {
            throw new IllegalStateException("rental-business 联盟纠纷到期解冻接口返回空响应");
        }
        if (!R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 联盟纠纷到期解冻失败：" + response.getMsg());
        }
        log.info("联盟纠纷到期解冻任务完成：closed={}", response.getData());
    }
}
