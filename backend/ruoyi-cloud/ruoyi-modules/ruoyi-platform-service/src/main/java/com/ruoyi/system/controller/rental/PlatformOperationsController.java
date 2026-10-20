package com.ruoyi.system.controller.rental;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.system.model.rental.PlatformAlertEventQuery;
import com.ruoyi.system.model.rental.PlatformAlertEventResolveCommand;
import com.ruoyi.system.model.rental.PlatformBackupRecordCommand;
import com.ruoyi.system.model.rental.PlatformBackupRecordQuery;
import com.ruoyi.system.model.rental.PlatformCapacityQuery;
import com.ruoyi.system.model.rental.PlatformCapacitySnapshotCommand;
import com.ruoyi.system.model.rental.PlatformOperationsQuery;
import com.ruoyi.system.service.rental.PlatformAlertEventService;
import com.ruoyi.system.service.rental.PlatformOperationsLedgerService;
import com.ruoyi.system.service.rental.PlatformOperationsService;

/** 平台经营分析和运营风控接口。 */
@RestController
@RequestMapping("/rental/operations")
public class PlatformOperationsController extends BaseController
{
    private final PlatformOperationsService operationsService;
    private final PlatformAlertEventService alertEventService;
    private final PlatformOperationsLedgerService ledgerService;

    public PlatformOperationsController(PlatformOperationsService operationsService,
            PlatformAlertEventService alertEventService, PlatformOperationsLedgerService ledgerService)
    {
        this.operationsService = operationsService;
        this.alertEventService = alertEventService;
        this.ledgerService = ledgerService;
    }

    /** 查询经营趋势、商户排行和运营风险事项。 */
    @RequiresPermissions("platform:operations:overview")
    @GetMapping("/overview")
    public AjaxResult overview(PlatformOperationsQuery query)
    {
        assertPlatformClient();
        return success(operationsService.overview(query));
    }

    /** 确认平台运营风险，确认后仍会保留风险事件和后续观察记录。 */
    @Log(title = "运营风险确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("platform:operations:risk:ack")
    @PostMapping("/risks/{riskEventId}/acknowledge")
    public AjaxResult acknowledgeRisk(@PathVariable Long riskEventId)
    {
        assertPlatformClient();
        operationsService.acknowledgeRisk(riskEventId);
        return success();
    }

    /** 分页查询平台运维告警事件。 */
    @RequiresPermissions("platform:operations:alert:list")
    @GetMapping("/alerts")
    public TableDataInfo alerts(PlatformAlertEventQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(alertEventService.list(query));
    }

    /** 确认一条待处理运维告警。 */
    @Log(title = "运维告警确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("platform:operations:alert:ack")
    @PostMapping("/alerts/{alertEventId}/acknowledge")
    public AjaxResult acknowledgeAlert(@PathVariable Long alertEventId)
    {
        assertPlatformClient();
        alertEventService.acknowledge(alertEventId);
        return success();
    }

    /** 解除一条运维告警并保留人工说明。 */
    @Log(title = "运维告警解除", businessType = BusinessType.UPDATE)
    @RequiresPermissions("platform:operations:alert:resolve")
    @PostMapping("/alerts/{alertEventId}/resolve")
    public AjaxResult resolveAlert(@PathVariable Long alertEventId,
            @RequestBody(required = false) PlatformAlertEventResolveCommand command)
    {
        assertPlatformClient();
        alertEventService.resolve(alertEventId, command == null ? null : command.getRemark());
        return success();
    }

    /** 查询备份和容量台账摘要。 */
    @RequiresPermissions("platform:operations:ledger:view")
    @GetMapping("/ledger/summary")
    public AjaxResult ledgerSummary()
    {
        assertPlatformClient();
        return success(ledgerService.summary());
    }

    /** 分页查询平台备份记录。 */
    @RequiresPermissions("platform:operations:ledger:view")
    @GetMapping("/backups")
    public TableDataInfo backups(PlatformBackupRecordQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(ledgerService.listBackups(query));
    }

    /** 登记一次备份执行结果。 */
    @Log(title = "登记备份记录", businessType = BusinessType.INSERT)
    @RequiresPermissions("platform:operations:backup:add")
    @PostMapping("/backups")
    public AjaxResult registerBackup(@RequestBody PlatformBackupRecordCommand command)
    {
        assertPlatformClient();
        ledgerService.registerBackup(command);
        return success();
    }

    /** 分页查询平台容量流水。 */
    @RequiresPermissions("platform:operations:ledger:view")
    @GetMapping("/capacity")
    public TableDataInfo capacity(PlatformCapacityQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(ledgerService.listCapacity(query));
    }

    /** 登记一条容量快照并计算相对上次快照的变化。 */
    @Log(title = "登记容量快照", businessType = BusinessType.INSERT)
    @RequiresPermissions("platform:operations:capacity:add")
    @PostMapping("/capacity")
    public AjaxResult registerCapacity(@RequestBody PlatformCapacitySnapshotCommand command)
    {
        assertPlatformClient();
        ledgerService.recordCapacity(command);
        return success();
    }

    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("当前接口仅支持平台端访问");
        }
    }
}
