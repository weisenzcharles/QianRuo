package com.ruoyi.rentalbusiness.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantAllianceScopeCommand;
import com.ruoyi.rentalbusiness.service.MerchantAllianceScopeService;

/** 商户联盟共享范围接口。 */
@RestController
@RequestMapping("/merchant/alliance-scopes")
public class MerchantAllianceScopeController
{
    private final MerchantAllianceScopeService service;

    public MerchantAllianceScopeController(MerchantAllianceScopeService service)
    {
        this.service = service;
    }

    /** 查询当前商户的共享仓库和商品范围。 */
    @RequiresPermissions("merchant:alliance:list")
    @GetMapping
    public AjaxResult list()
    {
        return AjaxResult.success(service.list());
    }

    /** 保存共享仓库范围。 */
    @RequiresPermissions("merchant:alliance:list")
    @Log(title = "联盟共享仓库范围", businessType = BusinessType.UPDATE)
    @PostMapping("/warehouses")
    public AjaxResult saveWarehouse(@RequestBody MerchantAllianceScopeCommand command)
    {
        service.saveWarehouse(command);
        return AjaxResult.success();
    }

    /** 保存共享商品范围。 */
    @RequiresPermissions("merchant:alliance:list")
    @Log(title = "联盟共享商品范围", businessType = BusinessType.UPDATE)
    @PostMapping("/products")
    public AjaxResult saveProduct(@RequestBody MerchantAllianceScopeCommand command)
    {
        service.saveProduct(command);
        return AjaxResult.success();
    }

    /** 删除共享仓库范围。 */
    @RequiresPermissions("merchant:alliance:list")
    @DeleteMapping("/warehouses/{scopeId}")
    public AjaxResult deleteWarehouse(@PathVariable Long scopeId)
    {
        service.deleteWarehouse(scopeId);
        return AjaxResult.success();
    }

    /** 删除共享商品范围。 */
    @RequiresPermissions("merchant:alliance:list")
    @DeleteMapping("/products/{scopeId}")
    public AjaxResult deleteProduct(@PathVariable Long scopeId)
    {
        service.deleteProduct(scopeId);
        return AjaxResult.success();
    }
}
