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
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantInventoryAlertRuleCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventorySyncTokenCreateCommand;
import com.ruoyi.rentalbusiness.service.MerchantInventoryGovernanceService;

/** 商户库存告警和外部同步授权接口。 */
@RestController
@RequestMapping("/merchant/inventory/governance")
public class MerchantInventoryGovernanceController extends BaseController
{
    private final MerchantInventoryGovernanceService service;

    public MerchantInventoryGovernanceController(MerchantInventoryGovernanceService service)
    {
        this.service = service;
    }

    /** 查询库存告警规则。 */
    @RequiresPermissions("merchant:inventory:alert:list")
    @GetMapping("/alerts/rules")
    public AjaxResult listRules(@RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId)
    {
        return success(service.listRules(warehouseId, productId));
    }

    /** 新增或修改库存告警规则。 */
    @RequiresPermissions("merchant:inventory:alert:edit")
    @Log(title = "库存告警规则", businessType = BusinessType.UPDATE)
    @PutMapping("/alerts/rules")
    public AjaxResult saveRule(@RequestBody MerchantInventoryAlertRuleCommand command)
    {
        return success(service.saveRule(command));
    }

    /** 删除库存告警规则。 */
    @RequiresPermissions("merchant:inventory:alert:edit")
    @Log(title = "库存告警规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/alerts/rules/{ruleId}")
    public AjaxResult deleteRule(@PathVariable Long ruleId)
    {
        service.deleteRule(ruleId);
        return success();
    }

    /** 查询并刷新库存告警事件。 */
    @RequiresPermissions("merchant:inventory:alert:list")
    @GetMapping("/alerts/events")
    public AjaxResult listEvents(@RequestParam(required = false) Integer alertStatus,
            @RequestParam(required = false) Long warehouseId)
    {
        return success(service.listEvents(alertStatus, warehouseId));
    }

    /** 确认待处理告警。 */
    @RequiresPermissions("merchant:inventory:alert:ack")
    @Log(title = "库存告警确认", businessType = BusinessType.UPDATE)
    @PostMapping("/alerts/events/{alertId}/ack")
    public AjaxResult acknowledge(@PathVariable Long alertId)
    {
        service.acknowledge(alertId);
        return success();
    }

    /** 查询外部库存同步令牌。 */
    @RequiresPermissions("merchant:inventory:sync:list")
    @GetMapping("/sync/tokens")
    public AjaxResult listSyncTokens()
    {
        return success(service.listSyncTokens());
    }

    /** 查询外部库存同步调用记录。 */
    @RequiresPermissions("merchant:inventory:sync:list")
    @GetMapping("/sync/logs")
    public AjaxResult listSyncLogs(@RequestParam(required = false) Long tokenId)
    {
        return success(service.listSyncLogs(tokenId));
    }

    /** 创建外部库存同步令牌。 */
    @RequiresPermissions("merchant:inventory:sync:edit")
    @Log(title = "外部库存同步授权", businessType = BusinessType.INSERT)
    @PostMapping("/sync/tokens")
    public AjaxResult createSyncToken(@RequestBody MerchantInventorySyncTokenCreateCommand command)
    {
        return success(service.createSyncToken(command));
    }

    /** 吊销外部库存同步令牌。 */
    @RequiresPermissions("merchant:inventory:sync:edit")
    @Log(title = "外部库存同步授权", businessType = BusinessType.UPDATE)
    @DeleteMapping("/sync/tokens/{tokenId}")
    public AjaxResult revokeSyncToken(@PathVariable Long tokenId)
    {
        service.revokeSyncToken(tokenId);
        return success();
    }
}
