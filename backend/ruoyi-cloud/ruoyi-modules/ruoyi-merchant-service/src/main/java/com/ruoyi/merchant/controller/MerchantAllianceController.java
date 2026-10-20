package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantAllianceService;
import com.ruoyi.merchant.model.MerchantAllianceCreateCommand;
import com.ruoyi.merchant.model.MerchantAllianceMemberCommand;

/** 商户租商联盟关系页面接口。 */
@RestController
@RequestMapping("/merchant/alliances")
public class MerchantAllianceController
{
    private final MerchantAllianceService service;

    public MerchantAllianceController(MerchantAllianceService service) { this.service = service; }

    @RequiresPermissions("merchant:alliance:list")
    @GetMapping
    public AjaxResult current() { return AjaxResult.success(service.current()); }

    @RequiresPermissions("merchant:alliance:list")
    @PostMapping
    public AjaxResult create(@RequestBody MerchantAllianceCreateCommand command)
    { return AjaxResult.success(service.create(command == null ? null : command.allianceName)); }

    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/members")
    public AjaxResult addMember(@RequestBody MerchantAllianceMemberCommand command)
    { service.addMember(command == null ? null : command.memberId); return AjaxResult.success(); }

    @RequiresPermissions("merchant:alliance:list")
    @GetMapping("/member-options")
    public AjaxResult memberOptions() { return AjaxResult.success(service.memberOptions()); }

    @RequiresPermissions("merchant:alliance:list")
    @PutMapping("/members/{memberId}")
    public AjaxResult updateMember(@PathVariable Long memberId, @RequestBody MerchantAllianceMemberCommand command)
    { service.updateMember(memberId, command == null ? null : command.shareStockEnabled,
            command == null ? null : command.acceptOrderLimit); return AjaxResult.success(); }

    @RequiresPermissions("merchant:alliance:list")
    @DeleteMapping("/members/{memberId}")
    public AjaxResult removeMember(@PathVariable Long memberId)
    { service.removeMember(memberId); return AjaxResult.success(); }
}
