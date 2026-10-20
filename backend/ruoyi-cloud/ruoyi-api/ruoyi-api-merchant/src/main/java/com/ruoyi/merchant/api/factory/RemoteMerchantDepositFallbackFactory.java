package com.ruoyi.merchant.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantDepositService;
import com.ruoyi.merchant.api.model.MerchantDepositBalance;
import com.ruoyi.merchant.api.model.MerchantDepositOperationCommand;

/** 商户保证金服务降级实现。 */
public class RemoteMerchantDepositFallbackFactory implements FallbackFactory<RemoteMerchantDepositService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantDepositFallbackFactory.class);

    @Override
    public RemoteMerchantDepositService create(Throwable cause)
    {
        log.error("商户保证金服务调用失败", cause);
        return new RemoteMerchantDepositService()
        {
            private R<MerchantDepositBalance> unavailable() { return R.fail("保证金服务暂不可用"); }
            @Override public R<MerchantDepositBalance> deposit(MerchantDepositOperationCommand c, String s, String t) { return unavailable(); }
            @Override public R<MerchantDepositBalance> freeze(MerchantDepositOperationCommand c, String s, String t) { return unavailable(); }
            @Override public R<MerchantDepositBalance> release(MerchantDepositOperationCommand c, String s, String t) { return unavailable(); }
            @Override public R<MerchantDepositBalance> deduct(MerchantDepositOperationCommand c, String s, String t) { return unavailable(); }
            @Override public R<MerchantDepositBalance> refund(MerchantDepositOperationCommand c, String s, String t) { return unavailable(); }
        };
    }
}
