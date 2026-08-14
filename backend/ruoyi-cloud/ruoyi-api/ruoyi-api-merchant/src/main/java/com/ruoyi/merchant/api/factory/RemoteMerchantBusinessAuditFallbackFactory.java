package com.ruoyi.merchant.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantBusinessAuditService;
import com.ruoyi.merchant.api.model.MerchantBusinessAuditCommand;

/** 商户业务审计内部契约降级处理。 */
@Component
public class RemoteMerchantBusinessAuditFallbackFactory implements FallbackFactory<RemoteMerchantBusinessAuditService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantBusinessAuditFallbackFactory.class);

    /**
     * 创建审计服务降级实现。
     *
     * @param cause 远程调用失败原因
     * @return 返回明确失败结果的审计契约实现
     */
    @Override
    public RemoteMerchantBusinessAuditService create(Throwable cause)
    {
        log.error("商户业务审计服务调用失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemoteMerchantBusinessAuditService()
        {
            /**
             * 远程审计不可用时返回失败结果，由调用方记录本地错误并保证主业务行为不被改变。
             *
             * @param command 业务审计命令
             * @param source 内部服务调用标识
             * @return 审计失败结果
             */
            @Override
            public R<Boolean> record(MerchantBusinessAuditCommand command, String source)
            {
                return R.fail("商户业务审计服务暂不可用");
            }
        };
    }
}
