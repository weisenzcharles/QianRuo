package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemotePlatformGovernanceFallbackFactory;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;
import com.ruoyi.system.api.model.MerchantCreditAppealCommand;
import com.ruoyi.system.api.model.MerchantCreditAppealView;
import com.ruoyi.system.api.model.MerchantCreditRiskView;
import com.ruoyi.system.api.model.PlatformExternalInterfaceAlertCommand;

/** 平台调度服务访问平台治理后台任务的内部契约。 */
@FeignClient(contextId = "remotePlatformGovernanceService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemotePlatformGovernanceFallbackFactory.class)
public interface RemotePlatformGovernanceService
{
    /** 平台治理内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/platform/governance";

    /** 清理已超过 30 天保留期限的回收站记录。 */
    @PostMapping(INTERNAL_PREFIX + "/recycle/cleanup")
    R<Integer> cleanupExpiredRecycleRecords(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 同步最近 24 小时调度失败记录，生成或恢复平台运维告警事件。 */
    @PostMapping(INTERNAL_PREFIX + "/operations/alerts/sync")
    R<Integer> synchronizeScheduledTaskAlerts(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 采集平台服务配置路径的文件系统容量，并写入平台容量台账。 */
    @PostMapping(INTERNAL_PREFIX + "/operations/capacity/collect")
    R<Integer> collectPlatformCapacity(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 写入或刷新外部接口告警，业务键相同的事件会被幂等合并。 */
    @PostMapping(INTERNAL_PREFIX + "/operations/external-interface-alerts")
    R<Void> upsertExternalInterfaceAlert(@org.springframework.web.bind.annotation.RequestBody
            PlatformExternalInterfaceAlertCommand command, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 外部接口恢复后解除对应告警。 */
    @PostMapping(INTERNAL_PREFIX + "/operations/external-interface-alerts/resolve")
    R<Void> resolveExternalInterfaceAlert(@org.springframework.web.bind.annotation.RequestParam("alertEventKey")
            String alertEventKey, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 创建或复用售后对应的平台客服工单。 */
    @PostMapping(INTERNAL_PREFIX + "/tickets/after-sales")
    R<PlatformAfterSalesTicketView> createAfterSalesTicket(@org.springframework.web.bind.annotation.RequestBody
            PlatformAfterSalesTicketCommand command, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询当前商户可见的信用摘要和信用流水。 */
    @org.springframework.web.bind.annotation.GetMapping(INTERNAL_PREFIX + "/members/{memberId}/credit-risk")
    R<MerchantCreditRiskView> getMerchantCreditRisk(
            @org.springframework.web.bind.annotation.PathVariable("memberId") Long memberId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 创建或复用商户信用流水申诉工单。 */
    @PostMapping(INTERNAL_PREFIX + "/members/{memberId}/credit-appeals")
    R<MerchantCreditAppealView> createMerchantCreditAppeal(
            @org.springframework.web.bind.annotation.PathVariable("memberId") Long memberId,
            @org.springframework.web.bind.annotation.RequestBody MerchantCreditAppealCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
