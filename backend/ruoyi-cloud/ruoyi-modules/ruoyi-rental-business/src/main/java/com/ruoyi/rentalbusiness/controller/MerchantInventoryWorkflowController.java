package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.InventoryReservationCommand;
import com.ruoyi.rentalbusiness.model.InventoryTransitionCommand;
import com.ruoyi.rentalbusiness.service.InventoryWorkflowService;

/** 商户库存状态机、预占、释放、过期和消耗接口。 */
@RestController
@RequestMapping("/merchant/inventory/workflow")
public class MerchantInventoryWorkflowController
{
    private final InventoryWorkflowService service;

    /** 注入库存工作流服务。 */
    public MerchantInventoryWorkflowController(InventoryWorkflowService service)
    {
        this.service = service;
    }

    /** 按套餐 BOM 预占库存。 */
    @RequiresPermissions("merchant:inventory:reserve")
    @Log(title = "库存预占", businessType = BusinessType.INSERT)
    @PostMapping("/reserve")
    public AjaxResult reserve(@RequestBody InventoryReservationCommand command)
    {
        return AjaxResult.success(service.reserve(command));
    }

    /** 查询预占详情。 */
    @RequiresPermissions("merchant:inventory:query")
    @GetMapping("/reservation/{reservationId}")
    public AjaxResult reservation(@PathVariable Long reservationId)
    {
        return AjaxResult.success(service.getReservation(reservationId));
    }

    /** 释放库存预占。 */
    @RequiresPermissions("merchant:inventory:reserve")
    @Log(title = "释放库存预占", businessType = BusinessType.UPDATE)
    @PostMapping("/reservation/{reservationId}/release")
    public AjaxResult release(@PathVariable Long reservationId, @RequestBody InventoryReservationCommand command)
    {
        return AjaxResult.success(service.release(reservationId, command == null ? null : command.reason,
                command == null ? null : command.idempotencyKey));
    }

    /** 将预占库存转为已消耗。 */
    @RequiresPermissions("merchant:inventory:reserve")
    @Log(title = "消耗库存预占", businessType = BusinessType.UPDATE)
    @PostMapping("/reservation/{reservationId}/consume")
    public AjaxResult consume(@PathVariable Long reservationId, @RequestBody InventoryReservationCommand command)
    {
        return AjaxResult.success(service.consume(reservationId, command == null ? null : command.reason,
                command == null ? null : command.idempotencyKey));
    }

    /** 将超时预占标记为 expired 并释放容量。 */
    @RequiresPermissions("merchant:inventory:reserve")
    @Log(title = "过期库存预占", businessType = BusinessType.UPDATE)
    @PostMapping("/reservation/{reservationId}/expire")
    public AjaxResult expire(@PathVariable Long reservationId, @RequestBody InventoryReservationCommand command)
    {
        return AjaxResult.success(service.expire(reservationId, command == null ? null : command.idempotencyKey));
    }

    /** 执行数量库存或逐件库存状态迁移。 */
    @RequiresPermissions("merchant:inventory:reserve")
    @Log(title = "库存状态迁移", businessType = BusinessType.UPDATE)
    @PostMapping("/transition")
    public AjaxResult transition(@RequestBody InventoryTransitionCommand command)
    {
        return AjaxResult.success(service.transition(command));
    }
}
