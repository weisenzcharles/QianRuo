package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantVerificationCommitCommand;
import com.ruoyi.rentalbusiness.model.MerchantVerificationScanCommand;
import com.ruoyi.rentalbusiness.service.MerchantVerificationService;

/** 商户极速核销台接口，负责扫码校验和批量提交。 */
@RestController
@RequestMapping("/merchant/verification")
public class MerchantVerificationController extends BaseController
{
    private final MerchantVerificationService service;

    public MerchantVerificationController(MerchantVerificationService service)
    {
        this.service = service;
    }

    /** 校验单次扫码，不修改订单和库存。 */
    @RequiresPermissions("merchant:verification:list")
    @PostMapping("/scan")
    public AjaxResult scan(@RequestBody MerchantVerificationScanCommand command)
    {
        return success(service.scan(command));
    }

    /** 提交扫码入库结果。 */
    @Log(title = "极速核销台入库", businessType = BusinessType.INSERT)
    @RequiresPermissions("merchant:verification:list")
    @PostMapping("/inbound/commit")
    public AjaxResult commitInbound(@RequestBody MerchantVerificationCommitCommand command)
    {
        return success(service.commitInbound(command));
    }

    /** 提交扫码出库结果。 */
    @Log(title = "极速核销台出库", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:verification:list")
    @PostMapping("/outbound/commit")
    public AjaxResult commitOutbound(@RequestBody MerchantVerificationCommitCommand command)
    {
        return success(service.commitOutbound(command));
    }
}
