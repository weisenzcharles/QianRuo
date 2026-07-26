package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteMerchantRegistrationService;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationCommand;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationVO;

/** 平台商户入驻内部接口降级处理。 */
@Component
public class RemoteMerchantRegistrationFallbackFactory implements FallbackFactory<RemoteMerchantRegistrationService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantRegistrationFallbackFactory.class);

    /**
     * 创建平台商户入驻接口的降级实现，统一返回服务不可用提示并保留原始异常日志。
     *
     * @param cause Feign 调用失败原因
     * @return 平台商户入驻接口降级实现
     */
    @Override
    public RemoteMerchantRegistrationService create(Throwable cause)
    {
        log.error("平台商户入驻服务调用失败", cause);
        return new RemoteMerchantRegistrationService()
        {
            @Override
            public R<MerchantRegistrationApplicationVO> submit(MerchantRegistrationApplicationCommand command,
                    String source)
            {
                return R.fail("平台商户入驻服务不可用");
            }

            @Override
            public R<MerchantRegistrationApplicationVO> getByPhone(String phone, String source)
            {
                return R.fail("平台商户入驻服务不可用");
            }

            @Override
            public R<MerchantRegistrationApplicationVO> resubmit(MerchantRegistrationApplicationCommand command,
                    String source)
            {
                return R.fail("平台商户入驻服务不可用");
            }
        };
    }
}
