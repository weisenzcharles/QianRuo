package com.ruoyi.merchant.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantFinanceService;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerCommand;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerResult;

/** 商户财务服务降级实现。 */
public class RemoteMerchantFinanceFallbackFactory implements FallbackFactory<RemoteMerchantFinanceService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantFinanceFallbackFactory.class);

    @Override
    public RemoteMerchantFinanceService create(Throwable cause)
    {
        log.error("商户财务服务调用失败", cause);
        return new RemoteMerchantFinanceService()
        {
            @Override
            public R<MerchantCompensationLedgerResult> postCompensation(MerchantCompensationLedgerCommand command,
                    String source, String clientType)
            {
                return R.fail("商户财务服务暂不可用");
            }
        };
    }
}
