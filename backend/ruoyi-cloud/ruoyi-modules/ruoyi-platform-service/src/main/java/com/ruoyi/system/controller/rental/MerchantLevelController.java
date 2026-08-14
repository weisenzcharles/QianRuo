package com.ruoyi.system.controller.rental;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.rental.MerchantLevel;
import com.ruoyi.system.service.rental.MerchantPromotionService;

/** 平台商户等级管理接口。 */
@RestController
@RequestMapping("/platform/merchant-level")
public class MerchantLevelController extends BaseController
{
    private final MerchantPromotionService service;

    /** @param service 商户等级与推广服务 */
    public MerchantLevelController(MerchantPromotionService service) { this.service = service; }

    /** @return 商户等级列表 */
    @RequiresPermissions("platform:merchantLevel:list")
    @GetMapping("/list")
    public AjaxResult list() { return success(service.listLevels()); }

    /** @param levelId 等级ID @return 等级详情 */
    @RequiresPermissions("platform:merchantLevel:query")
    @GetMapping("/{levelId}")
    public AjaxResult getInfo(@PathVariable Long levelId) { return success(service.getLevel(levelId)); }

    /** @param level 新等级 @return 新增结果 */
    @Log(title = "商户等级", businessType = BusinessType.INSERT)
    @RequiresPermissions("platform:merchantLevel:add")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody MerchantLevel level) { return toAjax(service.addLevel(level, SecurityUtils.getUsername())); }

    /** @param level 等级配置 @return 修改结果 */
    @Log(title = "商户等级", businessType = BusinessType.UPDATE)
    @RequiresPermissions("platform:merchantLevel:edit")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody MerchantLevel level) { return toAjax(service.updateLevel(level, SecurityUtils.getUsername())); }

    /** @param levelId 等级ID @return 删除结果 */
    @Log(title = "商户等级", businessType = BusinessType.DELETE)
    @RequiresPermissions("platform:merchantLevel:remove")
    @DeleteMapping("/{levelId}")
    public AjaxResult remove(@PathVariable Long levelId) { return toAjax(service.removeLevel(levelId, SecurityUtils.getUsername())); }
}
