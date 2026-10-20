package com.ruoyi.merchant.controller.internal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.merchant.api.MerchantServiceConstants;
import com.ruoyi.merchant.api.model.MerchantBusinessAuditCommand;
import com.ruoyi.merchant.application.MerchantBusinessAuditService;

/** 租赁业务服务和认证中心写入商户业务审计的内部入口。 */
@RestController
@RequestMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/business-audits")
public class InternalMerchantBusinessAuditController
{
    private final MerchantBusinessAuditService auditService;

    /** 注入商户业务审计服务。 */
    public InternalMerchantBusinessAuditController(MerchantBusinessAuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * 接收可信内部服务提交的商户业务审计记录。
     *
     * @param command 业务审计命令
     * @return 写入结果
     */
    @InnerAuth
    @PostMapping
    public R<Boolean> record(@RequestBody MerchantBusinessAuditCommand command)
    {
        return R.ok(auditService.record(command) > 0);
    }
}
