package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.rentalbusiness.model.ProtocolAcceptanceCommand;
import com.ruoyi.rentalbusiness.service.ProtocolService;

/** 商户协议查询与确认接口。平台协议维护入口位于 platform-service。 */
@RestController
@RequestMapping("/merchant/protocol")
public class MerchantProtocolController
{
    private final ProtocolService service;

    /** 注入协议版本服务。 */
    public MerchantProtocolController(ProtocolService service)
    {
        this.service = service;
    }

    /** 查询当前商户有效协议版本。 */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "protocolKey", required = false) String protocolKey,
            @RequestParam(value = "scopeType", required = false) Integer scopeType)
    {
        return AjaxResult.success(service.listMerchant(protocolKey, scopeType));
    }

    /** 确认当前商户有效协议版本。 */
    @Log(title = "商户协议确认", businessType = BusinessType.GRANT)
    @PostMapping("/accept")
    public AjaxResult accept(@RequestBody ProtocolAcceptanceCommand command)
    {
        return AjaxResult.success(service.accept(command));
    }
}
