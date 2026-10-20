package com.ruoyi.system.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteBrandModelFallbackFactory;
import com.ruoyi.system.api.model.BrandModel;

/** 商户服务访问平台标准品牌型号的内部契约。 */
@FeignClient(contextId = "remoteBrandModelService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteBrandModelFallbackFactory.class)
public interface RemoteBrandModelService
{
    /** 平台标准品牌型号内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/platform/brand-models";

    /**
     * 查询平台当前正常且未删除的标准品牌型号。
     *
     * @param source 内部服务调用标识
     * @return 平台标准品牌型号集合
     */
    @GetMapping(INTERNAL_PREFIX)
    R<List<BrandModel>> listActive(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
