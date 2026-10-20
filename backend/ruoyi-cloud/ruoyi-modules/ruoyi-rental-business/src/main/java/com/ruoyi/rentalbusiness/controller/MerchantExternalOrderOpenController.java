package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.rentalbusiness.model.MerchantExternalOrderCreateCommand;
import com.ruoyi.rentalbusiness.service.MerchantInventoryGovernanceService;

/** 外部平台正式订单接口，使用库存同步令牌和请求签名完成鉴权。 */
@RestController
@RequestMapping("/open")
public class MerchantExternalOrderOpenController
{
    private final MerchantInventoryGovernanceService service;

    public MerchantExternalOrderOpenController(MerchantInventoryGovernanceService service)
    {
        this.service = service;
    }

    /** 基于已预占库存创建正式订单；仓库、套餐/商品和数量以后端预占记录为准。 */
    @PostMapping("/orders")
    public AjaxResult createOrder(@RequestHeader(value = "X-Inventory-Token", required = false) String token,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestHeader(value = "X-Inventory-Signature", required = false) String signature,
            @RequestBody MerchantExternalOrderCreateCommand command)
    {
        return AjaxResult.success(service.createExternalOrder(token, requestId, signature, command,
                "/open/orders"));
    }
}
