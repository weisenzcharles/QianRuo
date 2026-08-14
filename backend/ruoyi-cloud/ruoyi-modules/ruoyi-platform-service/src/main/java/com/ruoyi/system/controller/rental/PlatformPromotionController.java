package com.ruoyi.system.controller.rental;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.service.rental.MerchantPromotionService;

/** 平台推广管理接口。 */
@RestController
@RequestMapping("/platform/promotion")
public class PlatformPromotionController extends BaseController
{
    private final MerchantPromotionService service;
    /** @param service 推广服务 */
    public PlatformPromotionController(MerchantPromotionService service) { this.service = service; }
    /** @return 全部商户邀请码和邀请明细 */
    @RequiresPermissions("platform:promotion:list")
    @GetMapping("/list")
    public AjaxResult list() { return success(service.listPromotions()); }
}
