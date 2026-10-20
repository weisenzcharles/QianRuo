package com.ruoyi.merchant.api;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.factory.RemoteMerchantFinanceFallbackFactory;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerCommand;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/** 商户财务账户内部服务契约。 */
@FeignClient(contextId = "remoteMerchantFinanceService", value = MerchantServiceConstants.SERVICE_NAME,
        fallbackFactory = RemoteMerchantFinanceFallbackFactory.class)
public interface RemoteMerchantFinanceService
{
    /** 将已审核的物流赔付幂等入账到租金/分账资金账户。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/finance/compensation")
    R<MerchantCompensationLedgerResult> postCompensation(
            @RequestBody MerchantCompensationLedgerCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);
}
