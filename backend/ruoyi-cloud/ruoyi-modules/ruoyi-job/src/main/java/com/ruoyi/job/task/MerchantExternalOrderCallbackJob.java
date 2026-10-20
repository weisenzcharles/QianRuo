package com.ruoyi.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;

/** 统一调度服务中的外部正式订单回调出站任务入口。 */
@Component("merchantExternalOrderCallbackJob")
public class MerchantExternalOrderCallbackJob
{
    private static final Logger log = LoggerFactory.getLogger(MerchantExternalOrderCallbackJob.class);
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public MerchantExternalOrderCallbackJob(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 调用 rental-business 发送到期回调事件，并让调度日志记录远程失败。 */
    public void dispatch()
    {
        log.info("外部正式订单回调发送任务开始");
        R<Integer> response = remoteRentalBusinessService.dispatchExternalOrderCallbacks(SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new IllegalStateException("rental-business 外部正式订单回调发送失败："
                    + (response == null ? "返回空响应" : response.getMsg()));
        }
        log.info("外部正式订单回调发送任务完成，送达数={}", response.getData());
    }
}
