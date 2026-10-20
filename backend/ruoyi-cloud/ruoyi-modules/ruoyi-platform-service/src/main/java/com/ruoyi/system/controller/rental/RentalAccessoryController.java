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
import com.ruoyi.system.model.rental.PlatformAccessoryCommand;
import com.ruoyi.system.model.rental.PlatformAccessoryQuery;
import com.ruoyi.system.service.rental.RentalAccessoryService;

/** 平台配件管理入口。 */
@RestController
@RequestMapping("/rental/accessory")
public class RentalAccessoryController extends BaseController
{
    private final RentalAccessoryService accessoryService;
    public RentalAccessoryController(RentalAccessoryService accessoryService) { this.accessoryService = accessoryService; }

    @RequiresPermissions("platform:accessory:list")
    @GetMapping("/list")
    public TableDataInfo list(@Validated PlatformAccessoryQuery query) { assertPlatformClient(); startPage(); return getDataTable(accessoryService.list(query)); }

    @RequiresPermissions("platform:accessory:query")
    @GetMapping("/{accessoryId}")
    public AjaxResult get(@PathVariable Long accessoryId) { assertPlatformClient(); return success(accessoryService.get(accessoryId)); }

    @RequiresPermissions("platform:accessory:add")
    @Log(title = "配件管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PlatformAccessoryCommand command) { assertPlatformClient(); return toAjax(accessoryService.add(command, SecurityUtils.getUserId())); }

    @RequiresPermissions("platform:accessory:edit")
    @Log(title = "配件管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody PlatformAccessoryCommand command) { assertPlatformClient(); return toAjax(accessoryService.update(command, SecurityUtils.getUserId())); }

    @RequiresPermissions("platform:accessory:remove")
    @Log(title = "配件管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{accessoryId}")
    public AjaxResult delete(@PathVariable Long accessoryId) { assertPlatformClient(); return toAjax(accessoryService.delete(accessoryId, SecurityUtils.getUserId())); }

    private void assertPlatformClient() { if (!ClientContextHolder.isPlatformClient()) throw new ServiceException("商户端不允许访问平台配件管理接口"); }
}
