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
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.OrderChannelCommand;
import com.ruoyi.rentalbusiness.service.OrderChannelService;

/** 平台联盟订单渠道管理接口。 */
@RestController
@RequestMapping("/platform/order-channel")
public class PlatformOrderChannelController
{
    private final OrderChannelService service;

    /** 注入订单渠道服务。 */
    public PlatformOrderChannelController(OrderChannelService service)
    {
        this.service = service;
    }

    /** 查询平台联盟渠道。 */
    @RequiresPermissions("platform:orderChannel:list")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status)
    {
        return AjaxResult.success(service.listPlatformChannels(keyword, status));
    }

    /** 新增平台联盟渠道。 */
    @RequiresPermissions("platform:orderChannel:add")
    @Log(title = "平台联盟订单渠道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OrderChannelCommand command)
    {
        return AjaxResult.success(service.addChannel(command));
    }

    /** 修改平台联盟渠道。 */
    @RequiresPermissions("platform:orderChannel:edit")
    @Log(title = "平台联盟订单渠道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderChannelCommand command)
    {
        return AjaxResult.success(service.updatePlatformChannel(command));
    }

    /** 软删除平台联盟渠道。 */
    @RequiresPermissions("platform:orderChannel:remove")
    @Log(title = "平台联盟订单渠道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{channelId}")
    public AjaxResult remove(@PathVariable Long channelId)
    {
        return AjaxResult.success(service.deletePlatformChannel(channelId));
    }
}
