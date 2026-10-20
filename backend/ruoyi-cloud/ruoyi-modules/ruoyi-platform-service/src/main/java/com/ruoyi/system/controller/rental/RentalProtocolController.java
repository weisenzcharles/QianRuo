package com.ruoyi.system.controller.rental;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.model.rental.PlatformProtocolCommand;
import com.ruoyi.system.service.rental.RentalProtocolService;

/** 平台协议版本管理入口。 */
@RestController
@RequestMapping("/rental/protocol")
public class RentalProtocolController extends BaseController
{
    private final RentalProtocolService protocolService;

    /** 注入平台协议版本应用服务。 */
    public RentalProtocolController(RentalProtocolService protocolService)
    {
        this.protocolService = protocolService;
    }

    /** 查询平台协议全部版本。 */
    @RequiresPermissions("platform:protocol:list")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "protocolKey", required = false) String protocolKey)
    {
        assertPlatformClient();
        return success(protocolService.list(protocolKey));
    }

    /** 新增平台协议草稿。 */
    @RequiresPermissions("platform:protocol:add")
    @Log(title = "平台协议版本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PlatformProtocolCommand command)
    {
        assertPlatformClient();
        return success(protocolService.add(command));
    }

    /** 修改平台协议草稿。 */
    @RequiresPermissions("platform:protocol:edit")
    @Log(title = "平台协议版本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody PlatformProtocolCommand command)
    {
        assertPlatformClient();
        return success(protocolService.update(command));
    }

    /** 发布指定平台协议版本。 */
    @RequiresPermissions("platform:protocol:publish")
    @Log(title = "发布平台协议版本", businessType = BusinessType.UPDATE)
    @PostMapping("/{protocolId}/publish")
    public AjaxResult publish(@PathVariable("protocolId") Long protocolId,
            @RequestParam(value = "effectiveTime", required = false) String effectiveTime)
    {
        assertPlatformClient();
        return success(protocolService.publish(protocolId, effectiveTime));
    }

    /** 校验请求来自平台终端，防止商户终端直接调用平台协议接口。 */
    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台协议接口");
        }
    }
}
