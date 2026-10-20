package com.ruoyi.system.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteAccessoryFallbackFactory;
import com.ruoyi.system.api.model.PlatformAccessoryOption;

/** 商户套餐读取平台官方配件的内部契约。 */
@FeignClient(contextId = "remoteAccessoryService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteAccessoryFallbackFactory.class)
public interface RemoteAccessoryService
{
    String INTERNAL_PREFIX = "/internal/platform/accessories";

    @GetMapping(INTERNAL_PREFIX)
    R<List<PlatformAccessoryOption>> listActive(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
