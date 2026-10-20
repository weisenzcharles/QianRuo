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
import com.ruoyi.system.model.rental.PlatformCompensationRuleCommand;
import com.ruoyi.system.service.rental.RentalCompensationRuleService;

/** 平台联盟赔付规则模板管理入口。 */
@RestController
@RequestMapping("/rental/compensation-rule")
public class RentalCompensationRuleController extends BaseController
{
    private final RentalCompensationRuleService service;
    public RentalCompensationRuleController(RentalCompensationRuleService service) { this.service = service; }
    @RequiresPermissions("platform:compensationRule:list")
    @GetMapping("/list") public TableDataInfo list() { assertPlatform(); return getDataTable(service.list()); }
    @RequiresPermissions("platform:compensationRule:query")
    @GetMapping("/{ruleId}") public AjaxResult get(@PathVariable Long ruleId) { assertPlatform(); return success(service.get(ruleId)); }
    @RequiresPermissions("platform:compensationRule:add") @Log(title = "赔付规则模板", businessType = BusinessType.INSERT)
    @PostMapping public AjaxResult add(@Validated @RequestBody PlatformCompensationRuleCommand command) { assertPlatform(); return toAjax(service.add(command, SecurityUtils.getUserId())); }
    @RequiresPermissions("platform:compensationRule:edit") @Log(title = "赔付规则模板", businessType = BusinessType.UPDATE)
    @PutMapping public AjaxResult update(@Validated @RequestBody PlatformCompensationRuleCommand command) { assertPlatform(); return toAjax(service.update(command, SecurityUtils.getUserId())); }
    @RequiresPermissions("platform:compensationRule:remove") @Log(title = "赔付规则模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleId}") public AjaxResult delete(@PathVariable Long ruleId) { assertPlatform(); return toAjax(service.delete(ruleId, SecurityUtils.getUserId())); }
    private void assertPlatform() { if (!ClientContextHolder.isPlatformClient()) throw new ServiceException("商户端不允许访问平台赔付规则"); }
}
