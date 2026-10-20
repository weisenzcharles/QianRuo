package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.model.MerchantInvitationOverviewVO;

/** 商户服务访问平台邀请码数据的内部契约。 */
@FeignClient(contextId = "remoteMerchantPromotionService", value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteMerchantPromotionService
{
    String INTERNAL_PREFIX = "/internal/platform/merchant-promotion";

    /**
     * 查询当前商户的邀请码与邀请记录。
     *
     * @param tenantId 服务端认证上下文中的租户唯一编号
     * @param memberId 服务端认证上下文中的商户唯一编号
     * @param source 内部调用标识
     * @return 邀请概览
     */
    @GetMapping(INTERNAL_PREFIX + "/overview")
    R<MerchantInvitationOverviewVO> overview(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
