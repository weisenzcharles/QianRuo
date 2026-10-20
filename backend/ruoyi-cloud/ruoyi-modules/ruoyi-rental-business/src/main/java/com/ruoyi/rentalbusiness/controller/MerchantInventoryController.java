package com.ruoyi.rentalbusiness.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.ruoyi.rentalbusiness.model.MerchantInventoryBatchCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventoryForecastQuery;
import com.ruoyi.rentalbusiness.model.MerchantInventoryUnitTransferCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventoryUnitUpdateCommand;
import com.ruoyi.rentalbusiness.service.MerchantInventoryService;
import com.ruoyi.rentalbusiness.service.MerchantInventoryForecastService;

/** 商户新架构库存接口，支持逐件序列号和数量库存。 */
@RestController
@RequestMapping("/merchant/inventory")
public class MerchantInventoryController extends BaseController
{
    private final MerchantInventoryService service;
    private final MerchantInventoryForecastService forecastService;

    public MerchantInventoryController(MerchantInventoryService service, MerchantInventoryForecastService forecastService)
    {
        this.service = service;
        this.forecastService = forecastService;
    }

    /** 查询库存汇总。 */
    @RequiresPermissions("merchant:inventory:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long warehouseId, @RequestParam(required = false) Integer unitStatus,
            @RequestParam(required = false) Long classId, @RequestParam(required = false) Integer productType,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long excludeWarehouseId)
    {
        startPage();
        return getDataTable(service.list(keyword, warehouseId, unitStatus, classId, productType, brand,
                excludeWarehouseId));
    }

    /** 查询联盟共享库存。 */
    @RequiresPermissions("merchant:alliance:list")
    @GetMapping("/shared")
    public TableDataInfo shared(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long warehouseId, @RequestParam(required = false) Integer productType)
    {
        startPage();
        return getDataTable(service.shared(keyword, warehouseId, productType));
    }

    /** 查询最近 30 天库存概况，支持按型号或套餐查看。 */
    @RequiresPermissions("merchant:trend:inventory")
    @GetMapping("/overview")
    public TableDataInfo overview(@RequestParam(defaultValue = "model") String mode,
            @RequestParam(required = false) Long warehouseId, @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long modelId, @RequestParam(defaultValue = "30") Integer days)
    {
        return getDataTable(service.overview(mode, warehouseId, brand, modelId, days));
    }

    /** 查询未来指定天数内按型号或套餐汇总的发货和归还趋势。 */
    @RequiresPermissions("merchant:trend:inbound-outbound")
    @GetMapping("/inbound-outbound-trend")
    public AjaxResult inboundOutboundTrend(@RequestParam(defaultValue = "model") String mode,
            @RequestParam(required = false) Long warehouseId, @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long modelId, @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(defaultValue = "false") boolean showAll)
    {
        return success(service.inboundOutboundTrend(mode, warehouseId, brand, modelId, days, showAll));
    }

    /** 分页查询收发趋势数字对应的订单明细。 */
    @RequiresPermissions("merchant:trend:inbound-outbound")
    @GetMapping("/inbound-outbound-trend/orders")
    public TableDataInfo inboundOutboundTrendOrders(@RequestParam String mode,
            @RequestParam(required = false) Long warehouseId, @RequestParam Long itemId,
            @RequestParam String trendDate, @RequestParam String direction, @RequestParam String source)
    {
        startPage();
        return getDataTable(service.inboundOutboundOrders(mode, warehouseId, itemId, trendDate, direction, source));
    }

    /** 查询未来指定天数的持久化库存预测快照。 */
    @RequiresPermissions("merchant:inventory:forecast:list")
    @GetMapping("/forecast")
    public AjaxResult forecast(MerchantInventoryForecastQuery query)
    {
        return success(forecastService.list(query));
    }

    /** 手工刷新当前商户未来三十天库存预测。 */
    @RequiresPermissions("merchant:inventory:forecast:refresh")
    @Log(title = "库存预测刷新", businessType = BusinessType.UPDATE)
    @PostMapping("/forecast/refresh")
    public AjaxResult refreshForecast()
    {
        return success(forecastService.refreshCurrentMerchant());
    }

    /** 查询当前商户可录入库存的设备和配件商品。 */
    @RequiresPermissions("merchant:inventory:add")
    @GetMapping("/options")
    public AjaxResult options(@RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer productType, @RequestParam(required = false) String brand,
            @RequestParam(required = false) String keyword)
    {
        return success(service.productOptions(classId, productType, brand, keyword));
    }

    /** 查询序列号明细。 */
    @RequiresPermissions("merchant:inventory:query")
    @GetMapping("/{inventoryId}/units")
    public TableDataInfo units(@PathVariable Long inventoryId, @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer unitStatus,
            @RequestParam(required = false, defaultValue = "false") Boolean transferableOnly)
    {
        startPage();
        return getDataTable(service.units(inventoryId, productId, warehouseId, keyword, unitStatus,
                transferableOnly));
    }

    /** 查询数量库存当前仍在预占中的订单。 */
    @RequiresPermissions("merchant:inventory:query")
    @GetMapping("/{inventoryId}/reservation-orders")
    public TableDataInfo reservationOrders(@PathVariable Long inventoryId)
    {
        startPage();
        return getDataTable(service.reservationOrders(inventoryId));
    }

    /** 修改库存设备档案，包括序列号、状态、规格、成色、价格、保险和备注。 */
    @RequiresPermissions("merchant:inventory:edit")
    @Log(title = "商户库存序列号", businessType = BusinessType.UPDATE)
    @PutMapping("/units")
    public AjaxResult updateUnit(@RequestBody MerchantInventoryUnitUpdateCommand command)
    {
        return success(service.updateUnit(command));
    }

    /** 删除未关联订单的序列号明细。 */
    @RequiresPermissions("merchant:inventory:remove")
    @Log(title = "商户库存序列号", businessType = BusinessType.DELETE)
    @DeleteMapping("/units/{unitId}")
    public AjaxResult deleteUnit(@PathVariable Long unitId)
    {
        return success(service.deleteUnit(unitId));
    }

    /** 将未关联订单的可用库存单元转移到其他仓库。 */
    @RequiresPermissions("merchant:inventory:transfer")
    @Log(title = "商户库存转仓", businessType = BusinessType.UPDATE)
    @PostMapping("/units/transfer")
    public AjaxResult transferUnit(@RequestBody MerchantInventoryUnitTransferCommand command)
    {
        return success(service.transferUnit(command));
    }

    /** 批量录入序列号或初始化数量库存。 */
    @RequiresPermissions("merchant:inventory:add")
    @Log(title = "商户序列号库存", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult addBatch(@RequestBody MerchantInventoryBatchCommand command)
    {
        return success(service.addBatch(command));
    }
}
