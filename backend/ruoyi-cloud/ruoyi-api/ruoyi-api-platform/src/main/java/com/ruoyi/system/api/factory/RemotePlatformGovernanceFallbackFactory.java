package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemotePlatformGovernanceService;
import com.ruoyi.system.api.model.MerchantCreditAppealCommand;
import com.ruoyi.system.api.model.MerchantCreditAppealView;
import com.ruoyi.system.api.model.MerchantCreditRiskView;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;
import com.ruoyi.system.api.model.PlatformExternalInterfaceAlertCommand;

/** 平台治理定时任务内部调用降级处理。 */
@Component
public class RemotePlatformGovernanceFallbackFactory implements FallbackFactory<RemotePlatformGovernanceService>
{
    private static final Logger log = LoggerFactory.getLogger(RemotePlatformGovernanceFallbackFactory.class);

    @Override
    public RemotePlatformGovernanceService create(Throwable cause)
    {
        log.error("平台治理清理任务调用失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemotePlatformGovernanceService()
        {
            @Override
            public R<Integer> cleanupExpiredRecycleRecords(String source)
            {
                return R.fail("平台治理服务暂不可用");
            }

            @Override
            public R<Integer> synchronizeScheduledTaskAlerts(String source)
            {
                return R.fail("平台治理服务暂不可用");
            }

            @Override
            public R<Integer> collectPlatformCapacity(String source)
            {
                return R.fail("平台治理服务暂不可用");
            }

            @Override
            public R<Void> upsertExternalInterfaceAlert(PlatformExternalInterfaceAlertCommand command, String source)
            {
                return R.fail("平台治理服务暂不可用");
            }

            @Override
            public R<Void> resolveExternalInterfaceAlert(String alertEventKey, String source)
            {
                return R.fail("平台治理服务暂不可用");
            }

            @Override
            public R<PlatformAfterSalesTicketView> createAfterSalesTicket(PlatformAfterSalesTicketCommand command,
                    String source)
            {
                return R.fail("平台治理服务暂不可用");
            }

            @Override
            public R<MerchantCreditRiskView> getMerchantCreditRisk(Long memberId, String source)
            {
                return R.fail("平台治理服务暂不可用");
            }

            @Override
            public R<MerchantCreditAppealView> createMerchantCreditAppeal(Long memberId,
                    MerchantCreditAppealCommand command, String source)
            {
                return R.fail("平台治理服务暂不可用");
            }
        };
    }
}
