package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantInvitationService;

/** 商户端我的邀请接口。 */
@RestController
@RequestMapping("/merchant/invitation")
public class MerchantInvitationController extends BaseController
{
    private final MerchantInvitationService service;
    /** @param service 商户邀请服务 */
    public MerchantInvitationController(MerchantInvitationService service) { this.service = service; }
    /** @return 当前商户邀请码、名额和邀请记录 */
    @RequiresPermissions("merchant:invitation:list")
    @GetMapping("/overview")
    public AjaxResult overview() { return success(service.overview()); }
}
