package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.rentalbusiness.model.MerchantExternalInventoryReservationCommand;
import com.ruoyi.rentalbusiness.service.MerchantInventoryGovernanceService;

/** 外部库存同步接口，令牌鉴权和签名校验由库存治理服务完成。 */
@RestController
@RequestMapping("/open/inventory")
public class MerchantInventorySyncOpenController
{
    private final MerchantInventoryGovernanceService service;

    public MerchantInventorySyncOpenController(MerchantInventoryGovernanceService service)
    {
        this.service = service;
    }

    /** 返回指定商户库存快照；原始同步令牌不进入 URL 和业务日志。 */
    @GetMapping("/snapshot")
    public AjaxResult snapshot(@RequestHeader(value = "X-Inventory-Token", required = false) String token,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestParam(required = false) Long warehouseId, @RequestParam(required = false) Long productId)
    {
        return AjaxResult.success(service.snapshot(token, requestId, warehouseId, productId, "/open/inventory/snapshot"));
    }

    /** 创建外部平台库存预占；签名原文由接口契约中的固定字段顺序组成。 */
    @PostMapping("/reservations")
    public AjaxResult reserve(@RequestHeader(value = "X-Inventory-Token", required = false) String token,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestHeader(value = "X-Inventory-Signature", required = false) String signature,
            @RequestBody MerchantExternalInventoryReservationCommand command)
    {
        return AjaxResult.success(service.reserveExternal(token, requestId, signature, command,
                "/open/inventory/reservations"));
    }

    /** 释放外部平台库存预占。 */
    @PostMapping("/reservations/{reservationNo}/release")
    public AjaxResult release(@RequestHeader(value = "X-Inventory-Token", required = false) String token,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestHeader(value = "X-Inventory-Signature", required = false) String signature,
            @PathVariable String reservationNo)
    {
        return AjaxResult.success(service.releaseExternal(token, requestId, signature, reservationNo,
                "/open/inventory/reservations/" + reservationNo + "/release"));
    }

}
