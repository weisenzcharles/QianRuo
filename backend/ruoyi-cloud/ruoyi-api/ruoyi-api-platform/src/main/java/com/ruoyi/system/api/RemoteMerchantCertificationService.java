package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteMerchantCertificationFallbackFactory;
import com.ruoyi.system.api.model.MerchantCertificationView;

/** 商户服务访问的平台认证资料内部契约。 */
@FeignClient(contextId = "remoteMerchantCertificationService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteMerchantCertificationFallbackFactory.class)
public interface RemoteMerchantCertificationService
{
    String INTERNAL_PREFIX = "/internal/platform/merchant-certification";

    /**
     * 按可信商户主键读取完整认证、会员类型和接单资格资料。
     *
     * @param memberId 由商户登录上下文解析出的商户主键
     * @param source 内部服务调用标识
     * @return 商户认证聚合资料
     */
    @GetMapping(INTERNAL_PREFIX + "/member/{memberId}")
    R<MerchantCertificationView> getByMemberId(@PathVariable("memberId") Long memberId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
