package com.ruoyi.rentalbusiness.controller;

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
import com.ruoyi.rentalbusiness.model.ProtocolAcceptanceCommand;
import com.ruoyi.rentalbusiness.model.ProtocolCommand;
import com.ruoyi.rentalbusiness.service.ProtocolService;

/** 平台协议版本管理与商户协议确认接口。 */
@RestController
public class ProtocolController
{
    private final ProtocolService service;

    /** 注入协议版本服务。 */
    public ProtocolController(ProtocolService service)
    {
        this.service = service;
    }

    /** 查询平台协议全部版本。 */
    @RequiresPermissions("platform:protocol:list")
    @GetMapping("/platform/protocol/list")
    public AjaxResult platformList(@RequestParam(required = false) String protocolKey)
    {
        return AjaxResult.success(service.listPlatform(protocolKey));
    }

    /** 新增平台协议草稿。 */
    @RequiresPermissions("platform:protocol:add")
    @Log(title = "平台协议版本", businessType = BusinessType.INSERT)
    @PostMapping("/platform/protocol")
    public AjaxResult add(@RequestBody ProtocolCommand command)
    {
        return AjaxResult.success(service.add(command));
    }

    /** 修改平台协议草稿。 */
    @RequiresPermissions("platform:protocol:edit")
    @Log(title = "平台协议版本", businessType = BusinessType.UPDATE)
    @PutMapping("/platform/protocol")
    public AjaxResult edit(@RequestBody ProtocolCommand command)
    {
        return AjaxResult.success(service.update(command));
    }

    /** 发布指定平台协议版本。 */
    @RequiresPermissions("platform:protocol:publish")
    @Log(title = "发布平台协议版本", businessType = BusinessType.UPDATE)
    @PostMapping("/platform/protocol/{protocolId}/publish")
    public AjaxResult publish(@PathVariable Long protocolId)
    {
        return AjaxResult.success(service.publish(protocolId));
    }

    /** 查询当前商户有效协议版本。 */
    @RequiresPermissions("merchant:protocol:list")
    @GetMapping("/merchant/protocol/list")
    public AjaxResult merchantList(@RequestParam(required = false) String protocolKey)
    {
        return AjaxResult.success(service.listMerchant(protocolKey));
    }

    /** 确认当前商户有效协议版本。 */
    @RequiresPermissions("merchant:protocol:accept")
    @Log(title = "商户协议确认", businessType = BusinessType.GRANT)
    @PostMapping("/merchant/protocol/accept")
    public AjaxResult accept(@RequestBody ProtocolAcceptanceCommand command)
    {
        return AjaxResult.success(service.accept(command));
    }
}
