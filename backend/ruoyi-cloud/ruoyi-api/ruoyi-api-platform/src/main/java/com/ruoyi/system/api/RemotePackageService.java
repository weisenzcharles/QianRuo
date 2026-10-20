package com.ruoyi.system.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemotePackageFallbackFactory;
import com.ruoyi.system.api.model.PlatformPackage;

/** 商户服务读取平台已发布套餐的内部契约。 */
@FeignClient(contextId = "remotePackageService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemotePackageFallbackFactory.class)
public interface RemotePackageService
{
    String INTERNAL_PREFIX = "/internal/platform/packages";
    @GetMapping(INTERNAL_PREFIX)
    R<List<PlatformPackage>> listActive(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
