package com.ruoyi.merchant.controller.internal;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerCommand;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerResult;
import com.ruoyi.merchant.application.MerchantCompensationLedgerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 租赁业务服务使用的商户财务内部入口。 */
@RestController
@RequestMapping("/internal/merchant/finance")
public class InternalMerchantFinanceController
{
    private final MerchantCompensationLedgerService service;

    public InternalMerchantFinanceController(MerchantCompensationLedgerService service)
    {
        this.service = service;
    }

    /** 幂等记录已审核物流赔付，不开放给浏览器端直接调用。 */
    @PostMapping("/compensation")
    public R<MerchantCompensationLedgerResult> postCompensation(
            @RequestBody MerchantCompensationLedgerCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_MERCHANT.equals(clientType))
        {
            return R.fail("无效的财务请求");
        }
        return R.ok(service.post(command));
    }
}
