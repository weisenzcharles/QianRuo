package com.ruoyi.rentalbusiness.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantPackageCommand;
import com.ruoyi.rentalbusiness.service.MerchantPackageService;
import com.ruoyi.system.api.model.PlatformPackage;

/** 新架构商户套餐接口。 */
@RestController
@RequestMapping("/merchant/package")
public class MerchantPackageController extends BaseController
{
    private final MerchantPackageService service;
    public MerchantPackageController(MerchantPackageService service) { this.service = service; }

    @RequiresPermissions("merchant:package:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status)
    {
        startPage(); return getDataTable(service.list(keyword, status));
    }

    @RequiresPermissions("merchant:package:list")
    @GetMapping("/platform-options")
    public AjaxResult platformOptions() { return success(service.platformOptions()); }

    @RequiresPermissions("merchant:package:query")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }

    @RequiresPermissions("merchant:package:add")
    @PostMapping("/copy-platform")
    public AjaxResult copyPlatform(@RequestBody MerchantPackageCommand command)
    {
        return success(service.copyPlatform(command));
    }

    @GetMapping("/accessory-options")
    public AjaxResult accessoryOptions(@RequestParam(required = false) Long modelId)
    {
        return success(service.accessoryOptions(modelId));
    }

    @RequiresPermissions("merchant:package:add")
    @PostMapping
    public AjaxResult add(@RequestBody MerchantPackageCommand command) { return success(service.add(command)); }

    @RequiresPermissions("merchant:package:edit")
    @PutMapping
    public AjaxResult update(@RequestBody MerchantPackageCommand command) { return success(service.update(command)); }

    @RequiresPermissions("merchant:package:remove")
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) { return toAjax(service.delete(id)); }
}
