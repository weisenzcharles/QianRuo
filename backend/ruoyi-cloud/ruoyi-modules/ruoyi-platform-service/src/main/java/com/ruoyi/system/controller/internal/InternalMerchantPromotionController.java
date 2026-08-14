package com.ruoyi.system.controller.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteMerchantPromotionService;
import com.ruoyi.system.api.model.MerchantInvitationOverviewVO;
import com.ruoyi.system.service.rental.MerchantPromotionService;

/** 商户服务读取推广数据的内部接口。 */
@RestController
@RequestMapping(RemoteMerchantPromotionService.INTERNAL_PREFIX)
public class InternalMerchantPromotionController
{
    private final MerchantPromotionService service;
    /** @param service 推广服务 */
    public InternalMerchantPromotionController(MerchantPromotionService service) { this.service = service; }
    /** @param tenantId 租户ID @param memberId 商户ID @return 邀请概览 */
    @InnerAuth
    @GetMapping("/overview")
    public R<MerchantInvitationOverviewVO> overview(@RequestParam Long tenantId, @RequestParam Long memberId)
    {
        return R.ok(service.overview(tenantId, memberId));
    }
}
