package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteGlobalConfigService;

/** 平台全局配置内部服务降级处理。 */
@Component
public class RemoteGlobalConfigFallbackFactory implements FallbackFactory<RemoteGlobalConfigService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteGlobalConfigFallbackFactory.class);

    /**
     * 创建明确失败的降级实现，调用方会使用本地默认值而不是伪造远程配置成功。
     *
     * @param cause 远程调用失败原因
     * @return 全局配置降级实现
     */
    @Override
    public RemoteGlobalConfigService create(Throwable cause)
    {
        log.error("平台全局配置服务调用失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemoteGlobalConfigService()
        {
            /**
             * 返回失败结果，配置客户端收到失败后使用调用方提供的默认值。
             *
             * @param configKey 配置键
             * @param source 内部服务调用标识
             * @return 配置服务不可用结果
             */
            @Override
            public R<String> getValue(String configKey, String source)
            {
                return R.fail("平台全局配置服务暂不可用");
            }
        };
    }
}
