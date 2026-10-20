package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantLogisticsQuery;
import com.ruoyi.rentalbusiness.service.MerchantLogisticsTrackingService;

/** 商户物流治理接口，负责物流台账、轨迹查看和人工刷新。 */
@RestController
@RequestMapping("/merchant/logistics")
public class MerchantLogisticsController extends BaseController
{
    private final MerchantLogisticsTrackingService logisticsService;

    public MerchantLogisticsController(MerchantLogisticsTrackingService logisticsService)
    {
        this.logisticsService = logisticsService;
    }

    /** 分页查询当前商户有权访问的物流记录。 */
    @RequiresPermissions("merchant:logistics:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantLogisticsQuery query)
    {
        startPage();
        return getDataTable(logisticsService.list(query));
    }

    /** 查询当前商户订单指定方向的标准化物流轨迹。 */
    @RequiresPermissions("merchant:logistics:list")
    @GetMapping("/track")
    public AjaxResult track(@RequestParam String orderNo, @RequestParam Integer direction)
    {
        return success(logisticsService.trackByOrderNo(orderNo, direction, false));
    }

    /** 强制重新查询供应商轨迹并更新物流快照，不改变订单履约状态。 */
    @Log(title = "物流轨迹刷新", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:logistics:list")
    @PostMapping("/refresh")
    public AjaxResult refresh(@RequestParam String orderNo, @RequestParam Integer direction)
    {
        return success(logisticsService.trackByOrderNo(orderNo, direction, true));
    }
}
