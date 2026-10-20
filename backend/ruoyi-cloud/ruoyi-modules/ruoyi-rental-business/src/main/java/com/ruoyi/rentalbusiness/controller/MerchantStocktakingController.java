package com.ruoyi.rentalbusiness.controller;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantStocktakingCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantStocktakingScanCommand;
import com.ruoyi.rentalbusiness.service.MerchantStocktakingService;

/** 商户库存盘点接口。 */
@RestController
@RequestMapping("/merchant/inventory-check")
public class MerchantStocktakingController extends BaseController
{
    private final MerchantStocktakingService service;

    public MerchantStocktakingController(MerchantStocktakingService service)
    {
        this.service = service;
    }

    /** 分页查询盘点单。 */
    @RequiresPermissions("merchant:inventory:check")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String mode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate beginDate,
            @RequestParam(required = false) LocalDate endDate)
    {
        startPage();
        List<Map<String, Object>> rows = service.list(warehouseId, status, mode, keyword, beginDate, endDate);
        return getDataTable(rows);
    }

    /** 查询盘点工作台汇总。 */
    @RequiresPermissions("merchant:inventory:check")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(required = false) Long warehouseId)
    {
        return success(service.summary(warehouseId));
    }

    /** 查询指定仓库内可用于盘点范围筛选的分类或型号。 */
    @RequiresPermissions("merchant:inventory:check")
    @GetMapping("/options")
    public AjaxResult scopeOptions(@RequestParam Long warehouseId, @RequestParam String scope)
    {
        return success(service.scopeOptions(warehouseId, scope));
    }

    /** 查询盘点单详情及明细。 */
    @RequiresPermissions("merchant:inventory:check")
    @GetMapping("/{stocktakingId}")
    public AjaxResult detail(@PathVariable Long stocktakingId)
    {
        return success(service.detail(stocktakingId));
    }

    /** 创建手工或扫码盘点单。 */
    @RequiresPermissions("merchant:inventory:check")
    @Log(title = "库存盘点", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@RequestBody MerchantStocktakingCreateCommand command)
    {
        return success(service.create(command));
    }

    /** 更新手工盘点数量。 */
    @RequiresPermissions("merchant:inventory:check")
    @Log(title = "库存盘点", businessType = BusinessType.UPDATE)
    @PutMapping("/{stocktakingId}/items/{itemId}")
    public AjaxResult count(@PathVariable Long stocktakingId, @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body)
    {
        service.count(stocktakingId, itemId, body == null ? null : body.get("countedQuantity"));
        return success();
    }

    /** 录入设备扫码结果。 */
    @RequiresPermissions("merchant:inventory:check")
    @Log(title = "扫码盘点", businessType = BusinessType.INSERT)
    @PostMapping("/{stocktakingId}/scan")
    public AjaxResult scan(@PathVariable Long stocktakingId, @RequestBody MerchantStocktakingScanCommand command)
    {
        return success(service.scan(stocktakingId, command));
    }

    /** 完成本次盘点。 */
    @RequiresPermissions("merchant:inventory:check")
    @Log(title = "库存盘点", businessType = BusinessType.UPDATE)
    @PostMapping("/{stocktakingId}/complete")
    public AjaxResult complete(@PathVariable Long stocktakingId)
    {
        service.complete(stocktakingId);
        return success();
    }

    /** 关闭未完成的盘点单。 */
    @RequiresPermissions("merchant:inventory:check")
    @Log(title = "库存盘点", businessType = BusinessType.UPDATE)
    @PostMapping("/{stocktakingId}/close")
    public AjaxResult close(@PathVariable Long stocktakingId)
    {
        service.close(stocktakingId);
        return success();
    }

    /** 确认已复核的手工盘点差异。 */
    @RequiresPermissions("merchant:inventory:check")
    @Log(title = "库存盘点差异复核", businessType = BusinessType.UPDATE)
    @PostMapping("/{stocktakingId}/review")
    public AjaxResult review(@PathVariable Long stocktakingId)
    {
        service.review(stocktakingId);
        return success();
    }
}
