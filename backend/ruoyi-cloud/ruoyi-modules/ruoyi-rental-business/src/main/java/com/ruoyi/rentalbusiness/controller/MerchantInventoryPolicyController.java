package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantInventoryPolicyCommand;
import com.ruoyi.rentalbusiness.service.MerchantInventoryGovernanceService;

/** 商户库存可接单口径接口。 */
@RestController
@RequestMapping("/merchant/inventory/policy")
public class MerchantInventoryPolicyController
{
    private final MerchantInventoryGovernanceService service;

    public MerchantInventoryPolicyController(MerchantInventoryGovernanceService service)
    {
        this.service = service;
    }

    /** 修改保护数量或总库存比例。 */
    @RequiresPermissions("merchant:inventory:edit")
    @Log(title = "库存可接单口径", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody MerchantInventoryPolicyCommand command)
    {
        return AjaxResult.success(service.updateInventoryPolicy(command));
    }
}
