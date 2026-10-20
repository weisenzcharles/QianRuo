package com.ruoyi.merchant.controller.internal;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.model.MerchantDepositBalance;
import com.ruoyi.merchant.api.model.MerchantDepositOperationCommand;
import com.ruoyi.merchant.application.MerchantDepositService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 租赁业务服务使用的保证金内部入口。 */
@RestController
@RequestMapping("/internal/merchant/deposits")
public class InternalMerchantDepositController
{
    private final MerchantDepositService service;

    public InternalMerchantDepositController(MerchantDepositService service) { this.service = service; }

    @PostMapping("/deposit")
    public R<MerchantDepositBalance> deposit(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    { return operate(command, source, clientType, 1); }

    @PostMapping("/freeze")
    public R<MerchantDepositBalance> freeze(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    { return operate(command, source, clientType, 2); }

    @PostMapping("/release")
    public R<MerchantDepositBalance> release(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    { return operate(command, source, clientType, 4); }

    @PostMapping("/deduct")
    public R<MerchantDepositBalance> deduct(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    { return operate(command, source, clientType, 3); }

    @PostMapping("/refund")
    public R<MerchantDepositBalance> refund(@RequestBody MerchantDepositOperationCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    { return operate(command, source, clientType, 5); }

    private R<MerchantDepositBalance> operate(MerchantDepositOperationCommand command, String source,
            String clientType, int direction)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_MERCHANT.equals(clientType))
            return R.fail("无效的保证金请求");
        return R.ok(service.operate(command, direction));
    }
}
