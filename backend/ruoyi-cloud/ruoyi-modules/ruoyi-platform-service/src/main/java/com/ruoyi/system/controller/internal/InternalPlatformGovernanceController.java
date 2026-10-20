package com.ruoyi.system.controller.internal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemotePlatformGovernanceService;
import com.ruoyi.system.api.model.MerchantCreditAppealCommand;
import com.ruoyi.system.api.model.MerchantCreditAppealView;
import com.ruoyi.system.api.model.MerchantCreditRecordView;
import com.ruoyi.system.api.model.MerchantCreditRiskView;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;
import com.ruoyi.system.api.model.PlatformExternalInterfaceAlertCommand;
import com.ruoyi.system.model.rental.PlatformMemberCreditRecordVO;
import com.ruoyi.system.model.rental.PlatformMemberRiskVO;
import com.ruoyi.system.service.rental.PlatformAlertEventService;
import com.ruoyi.system.service.rental.PlatformCapacityCollectionService;
import com.ruoyi.system.service.rental.PlatformGovernanceService;
import com.ruoyi.system.service.rental.PlatformMemberRiskService;

/** 平台治理后台任务内部接口。 */
@RestController
@RequestMapping(RemotePlatformGovernanceService.INTERNAL_PREFIX)
public class InternalPlatformGovernanceController
{
    private final PlatformGovernanceService governanceService;
    private final PlatformAlertEventService alertEventService;
    private final PlatformCapacityCollectionService capacityCollectionService;
    private final PlatformMemberRiskService riskService;

    public InternalPlatformGovernanceController(PlatformGovernanceService governanceService,
            PlatformAlertEventService alertEventService, PlatformCapacityCollectionService capacityCollectionService,
            PlatformMemberRiskService riskService)
    {
        this.governanceService = governanceService;
        this.alertEventService = alertEventService;
        this.capacityCollectionService = capacityCollectionService;
        this.riskService = riskService;
    }

    /** 清理已超过保留期限的回收站记录。 */
    @InnerAuth
    @PostMapping("/recycle/cleanup")
    public R<Integer> cleanupExpiredRecycleRecords()
    {
        return R.ok(governanceService.cleanupExpiredRecycleRecords());
    }

    /** 同步最近 24 小时调度失败记录。 */
    @InnerAuth
    @PostMapping("/operations/alerts/sync")
    public R<Integer> synchronizeScheduledTaskAlerts()
    {
        return R.ok(alertEventService.synchronizeScheduledTaskAlerts());
    }

    /** 采集平台服务配置路径的文件系统容量，并刷新低容量告警。 */
    @InnerAuth
    @PostMapping("/operations/capacity/collect")
    public R<Integer> collectPlatformCapacity()
    {
        return R.ok(capacityCollectionService.collect());
    }

    /** 写入或刷新外部接口告警。 */
    @InnerAuth
    @PostMapping("/operations/external-interface-alerts")
    public R<Void> upsertExternalInterfaceAlert(@org.springframework.web.bind.annotation.RequestBody
            PlatformExternalInterfaceAlertCommand command)
    {
        alertEventService.upsertExternalInterfaceAlert(command);
        return R.ok();
    }

    /** 外部接口恢复后解除对应告警。 */
    @InnerAuth
    @PostMapping("/operations/external-interface-alerts/resolve")
    public R<Void> resolveExternalInterfaceAlert(@org.springframework.web.bind.annotation.RequestParam("alertEventKey")
            String alertEventKey)
    {
        alertEventService.resolveExternalInterfaceAlert(alertEventKey);
        return R.ok();
    }

    /** 创建或复用售后对应的平台客服工单。 */
    @InnerAuth
    @PostMapping("/tickets/after-sales")
    public R<PlatformAfterSalesTicketView> createAfterSalesTicket(@org.springframework.web.bind.annotation.RequestBody
            PlatformAfterSalesTicketCommand command)
    {
        return R.ok(governanceService.createAfterSalesTicket(command));
    }

    /** 查询商户端可见的信用摘要和信用流水。 */
    @InnerAuth
    @org.springframework.web.bind.annotation.GetMapping("/members/{memberId}/credit-risk")
    public R<MerchantCreditRiskView> getMerchantCreditRisk(@org.springframework.web.bind.annotation.PathVariable Long memberId)
    {
        return R.ok(toMerchantCreditRiskView(riskService.get(memberId)));
    }

    /** 创建或复用商户信用流水申诉工单。 */
    @InnerAuth
    @PostMapping("/members/{memberId}/credit-appeals")
    public R<MerchantCreditAppealView> createMerchantCreditAppeal(
            @org.springframework.web.bind.annotation.PathVariable Long memberId,
            @org.springframework.web.bind.annotation.RequestBody MerchantCreditAppealCommand command)
    {
        return R.ok(governanceService.createCreditAppealTicket(memberId, command));
    }

    private MerchantCreditRiskView toMerchantCreditRiskView(PlatformMemberRiskVO source)
    {
        MerchantCreditRiskView target = new MerchantCreditRiskView();
        if (source == null)
        {
            return target;
        }
        target.setCreditScore(source.getCreditScore());
        target.setCreditLevel(source.getCreditLevel());
        target.setCreditLevelName(source.getCreditLevelName());
        for (PlatformMemberCreditRecordVO item : source.getCreditRecords())
        {
            MerchantCreditRecordView record = new MerchantCreditRecordView();
            record.setRecordNo(item.getRecordNo());
            record.setEventType(item.getEventType());
            record.setEventTypeName(item.getEventTypeName());
            record.setScoreBefore(item.getScoreBefore());
            record.setScoreChange(item.getScoreChange());
            record.setScoreAfter(item.getScoreAfter());
            record.setReason(item.getReason());
            record.setSourceNo(item.getSourceNo());
            record.setReversalOfRecordNo(item.getReversalOfRecordNo());
            record.setReversalRecordNo(item.getReversalRecordNo());
            record.setCreateTime(item.getCreateTime());
            target.getCreditRecords().add(record);
        }
        return target;
    }
}
