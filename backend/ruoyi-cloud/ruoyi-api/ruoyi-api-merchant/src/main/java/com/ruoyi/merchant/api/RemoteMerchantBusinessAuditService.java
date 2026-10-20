package com.ruoyi.merchant.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.factory.RemoteMerchantBusinessAuditFallbackFactory;
import com.ruoyi.merchant.api.model.MerchantBusinessAuditCommand;

/**
 * 租赁业务服务写入商户业务审计的内部契约。
 *
 * <p>商户业务审计表由 merchant-service 所属的租赁治理库维护，租赁业务服务不能跨库写入该表，
 * 因此统一通过本契约提交审计命令。</p>
 */
@FeignClient(contextId = "remoteMerchantBusinessAuditService", value = MerchantServiceConstants.SERVICE_NAME,
        fallbackFactory = RemoteMerchantBusinessAuditFallbackFactory.class)
public interface RemoteMerchantBusinessAuditService
{
    /**
     * 写入一条商户业务审计记录。
     *
     * @param command 业务审计命令
     * @param source 内部服务调用标识
     * @return 写入结果
     */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/business-audits")
    R<Boolean> record(@RequestBody MerchantBusinessAuditCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
