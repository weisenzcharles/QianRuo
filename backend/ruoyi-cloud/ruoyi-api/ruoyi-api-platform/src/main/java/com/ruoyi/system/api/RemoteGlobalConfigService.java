package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteGlobalConfigFallbackFactory;

/**
 * 商户业务服务读取平台全局配置的内部契约。
 *
 * <p>商户服务不直接连接平台数据库，所有配置读取通过平台内部接口完成；接口仅接受
 * 内部服务来源请求，避免把平台系统配置暴露给商户端浏览器。</p>
 */
@FeignClient(contextId = "remoteGlobalConfigService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteGlobalConfigFallbackFactory.class)
public interface RemoteGlobalConfigService
{
    /** 平台全局配置内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/platform/global-config";

    /**
     * 按配置键读取平台配置值。
     *
     * @param configKey 配置键
     * @param source 内部服务调用标识
     * @return 配置值
     */
    @GetMapping(INTERNAL_PREFIX + "/value")
    R<String> getValue(@RequestParam("configKey") String configKey,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
