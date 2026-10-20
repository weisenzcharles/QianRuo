package com.ruoyi.system.controller.rental;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.model.rental.PlatformPackageCommand;
import com.ruoyi.system.model.rental.PlatformPackageQuery;
import com.ruoyi.system.service.rental.RentalPackageService;

/** 平台官方套餐管理入口。 */
@RestController
@RequestMapping("/rental/package")
public class RentalPackageController extends BaseController
{
    private final RentalPackageService packageService;
    public RentalPackageController(RentalPackageService packageService) { this.packageService = packageService; }
    @RequiresPermissions("platform:package:list") @GetMapping("/list")
    public TableDataInfo list(@Validated PlatformPackageQuery query) { assertPlatformClient(); startPage(); return getDataTable(packageService.list(query)); }
    @RequiresPermissions("platform:package:query") @GetMapping("/{packageId}")
    public AjaxResult get(@PathVariable Long packageId) { assertPlatformClient(); return success(packageService.get(packageId)); }
    @RequiresPermissions("platform:package:add") @Log(title = "套餐管理", businessType = BusinessType.INSERT) @PostMapping
    public AjaxResult add(@Validated @RequestBody PlatformPackageCommand command) { assertPlatformClient(); return toAjax(packageService.add(command, SecurityUtils.getUserId())); }
    @RequiresPermissions("platform:package:edit") @Log(title = "套餐管理", businessType = BusinessType.UPDATE) @PutMapping
    public AjaxResult update(@Validated @RequestBody PlatformPackageCommand command) { assertPlatformClient(); return toAjax(packageService.update(command, SecurityUtils.getUserId())); }
    @RequiresPermissions("platform:package:edit") @Log(title = "套餐管理", businessType = BusinessType.UPDATE) @PostMapping("/{packageId}/publish")
    public AjaxResult publish(@PathVariable Long packageId) { assertPlatformClient(); return toAjax(packageService.publish(packageId, SecurityUtils.getUserId())); }
    @RequiresPermissions("platform:package:remove") @Log(title = "套餐管理", businessType = BusinessType.DELETE) @DeleteMapping("/{packageId}")
    public AjaxResult delete(@PathVariable Long packageId) { assertPlatformClient(); return toAjax(packageService.delete(packageId, SecurityUtils.getUserId())); }
    private void assertPlatformClient() { if (!ClientContextHolder.isPlatformClient()) throw new ServiceException("商户端不允许访问平台套餐管理接口"); }
}
