package com.ruoyi.merchant.api.factory;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantProductClassService;

/** 商户经营类目内部契约降级处理。 */
@Component
public class RemoteMerchantProductClassFallbackFactory implements FallbackFactory<RemoteMerchantProductClassService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantProductClassFallbackFactory.class);

    /**
     * 创建失败即拒绝的降级实现，远程类目服务不可用时不能放行商品写入。
     *
     * @param cause 远程调用异常
     * @return 拒绝类目校验的降级实现
     */
    @Override
    public RemoteMerchantProductClassService create(Throwable cause)
    {
        log.error("商户经营类目服务调用失败: {}", cause.getMessage());
        return new RemoteMerchantProductClassService()
        {
            /** 远程类目不可用时返回失败，调用方必须拒绝业务写入和列表查询。 */
            @Override
            public R<List<Long>> selectedIds(String source)
            {
                return R.fail("商户经营类目服务不可用");
            }

            /** 远程类目不可用时返回失败，调用方必须拒绝业务写入。 */
            @Override
            public R<Boolean> assertAllowed(Long classId, String source)
            {
                return R.fail("商户经营类目服务不可用");
            }
        };
    }
}
