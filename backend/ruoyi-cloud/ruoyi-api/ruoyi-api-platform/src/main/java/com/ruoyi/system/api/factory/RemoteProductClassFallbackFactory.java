package com.ruoyi.system.api.factory;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteProductClassService;
import com.ruoyi.system.api.model.ProductClass;

/** 平台产品类目内部服务降级处理。 */
@Component
public class RemoteProductClassFallbackFactory implements FallbackFactory<RemoteProductClassService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteProductClassFallbackFactory.class);

    /**
     * 在平台类目服务不可用时返回明确失败结果，禁止商户端使用过期或不完整的类目范围。
     *
     * @param cause 远程调用异常
     * @return 平台产品类目降级实现
     */
    @Override
    public RemoteProductClassService create(Throwable cause)
    {
        log.error("平台产品类目服务调用失败", cause);
        return new RemoteProductClassService()
        {
            /**
             * 返回平台类目服务不可用错误，不向商户端伪造空类目集合。
             *
             * @param source 内部服务调用标识
             * @return 失败结果
             */
            @Override
            public R<List<ProductClass>> listActive(String source)
            {
                return R.fail("平台产品类目服务暂不可用");
            }
        };
    }
}
