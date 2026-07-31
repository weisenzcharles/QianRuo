package com.ruoyi.system.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteProductClassFallbackFactory;
import com.ruoyi.system.api.model.ProductClass;

/** 商户服务访问平台产品类目的内部契约。 */
@FeignClient(contextId = "remoteProductClassService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteProductClassFallbackFactory.class)
public interface RemoteProductClassService
{
    /** 平台产品类目内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/platform/product-classes";

    /**
     * 查询平台全部正常类目，供商户服务校验经营范围并构建可选树。
     *
     * @param source 内部服务调用标识
     * @return 平台正常类目的扁平集合
     */
    @GetMapping(INTERNAL_PREFIX)
    R<List<ProductClass>> listActive(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
