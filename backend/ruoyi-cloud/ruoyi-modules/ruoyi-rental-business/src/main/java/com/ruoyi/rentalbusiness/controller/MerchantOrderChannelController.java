package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.ruoyi.rentalbusiness.model.OrderChannelCommand;
import com.ruoyi.rentalbusiness.service.OrderChannelService;

/** 商户订单渠道、套餐渠道绑定和订单渠道转移接口。 */
@RestController
@RequestMapping("/merchant/order-channel")
public class MerchantOrderChannelController extends BaseController
{
    private final OrderChannelService service;

    /** 注入订单渠道应用服务。 */
    public MerchantOrderChannelController(OrderChannelService service)
    {
        this.service = service;
    }

    /** 查询平台联盟渠道与当前商户自定义渠道。 */
    @RequiresPermissions("merchant:orderChannel:query")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status)
    {
        startPage();
        return getDataTable(service.listChannels(keyword, status));
    }

    /** 查询套餐绑定的渠道列表。 */
    @RequiresPermissions("merchant:orderChannel:query")
    @GetMapping("/package/{packageId}")
    public AjaxResult packageChannels(@PathVariable Long packageId)
    {
        return success(service.listPackageChannels(packageId));
    }

    /** 新增商户自定义渠道。 */
    @RequiresPermissions("merchant:orderChannel:add")
    @Log(title = "商户订单渠道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OrderChannelCommand command)
    {
        return success(service.addChannel(command));
    }

    /** 修改商户自定义渠道。 */
    @RequiresPermissions("merchant:orderChannel:edit")
    @Log(title = "商户订单渠道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderChannelCommand command)
    {
        return success(service.updateChannel(command));
    }

    /** 删除商户自定义渠道。 */
    @RequiresPermissions("merchant:orderChannel:edit")
    @Log(title = "商户订单渠道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{channelId}")
    public AjaxResult remove(@PathVariable Long channelId)
    {
        return toAjax(service.deleteChannel(channelId));
    }

    /** 将套餐默认渠道转移到目标渠道。 */
    @RequiresPermissions("merchant:orderChannel:transfer")
    @Log(title = "套餐订单渠道转移", businessType = BusinessType.UPDATE)
    @PostMapping("/package/transfer")
    public AjaxResult transferPackage(@RequestBody OrderChannelCommand command)
    {
        return success(service.transferPackageChannel(command));
    }

    /** 批量将订单转移到目标渠道。 */
    @RequiresPermissions("merchant:orderChannel:transfer")
    @Log(title = "订单渠道转移", businessType = BusinessType.UPDATE)
    @PostMapping("/orders/transfer")
    public AjaxResult transferOrders(@RequestBody OrderChannelCommand command)
    {
        return success(service.transferOrders(command));
    }
}
