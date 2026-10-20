package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteCodeRuleFallbackFactory;

/** 租赁业务服务访问平台统一编码分配器的内部契约。 */
@FeignClient(contextId = "remoteCodeRuleService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteCodeRuleFallbackFactory.class)
public interface RemoteCodeRuleService
{
    /** 平台统一编码内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/platform/code-rules";

    /**
     * 按编码类型原子分配下一个系统编码。
     *
     * @param codeType 编码类型键
     * @param source 内部服务调用标识
     * @return 根据平台规则生成的唯一编码
     */
    @GetMapping(INTERNAL_PREFIX + "/{codeType}/next")
    R<String> nextCode(@PathVariable("codeType") String codeType,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
