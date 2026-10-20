package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteMerchantCertificationService;
import com.ruoyi.system.api.model.MerchantCertificationView;
import com.ruoyi.system.api.model.MerchantCertificationSupplementCommand;

/** 商户认证资料内部接口降级处理。 */
@Component
public class RemoteMerchantCertificationFallbackFactory implements FallbackFactory<RemoteMerchantCertificationService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantCertificationFallbackFactory.class);

    /**
     * 创建认证资料接口降级实现，避免平台服务不可用时向商户端返回空成功数据。
     *
     * @param cause Feign 调用失败原因
     * @return 认证资料接口降级实现
     */
    @Override
    public RemoteMerchantCertificationService create(Throwable cause)
    {
        log.error("平台商户认证资料服务调用失败", cause);
        return new RemoteMerchantCertificationService()
        {
            @Override
            public R<MerchantCertificationView> getByMemberId(Long memberId, String source)
            {
                return R.fail("商户认证资料服务不可用");
            }

            @Override
            public R<MerchantCertificationView> submitSupplement(Long memberId,
                    MerchantCertificationSupplementCommand command, String source)
            {
                return R.fail("商户认证资料服务不可用");
            }
        };
    }
}
