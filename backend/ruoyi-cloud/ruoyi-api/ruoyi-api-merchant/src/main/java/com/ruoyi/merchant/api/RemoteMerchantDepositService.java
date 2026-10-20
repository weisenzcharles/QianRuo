package com.ruoyi.merchant.api;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.factory.RemoteMerchantDepositFallbackFactory;
import com.ruoyi.merchant.api.model.MerchantDepositBalance;
import com.ruoyi.merchant.api.model.MerchantDepositOperationCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/** 商户保证金账户内部服务契约。 */
@FeignClient(contextId = "remoteMerchantDepositService", value = MerchantServiceConstants.SERVICE_NAME,
        fallbackFactory = RemoteMerchantDepositFallbackFactory.class)
public interface RemoteMerchantDepositService
{
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/deposits/deposit")
    R<MerchantDepositBalance> deposit(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/deposits/freeze")
    R<MerchantDepositBalance> freeze(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/deposits/release")
    R<MerchantDepositBalance> release(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/deposits/deduct")
    R<MerchantDepositBalance> deduct(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/deposits/refund")
    R<MerchantDepositBalance> refund(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);
}
