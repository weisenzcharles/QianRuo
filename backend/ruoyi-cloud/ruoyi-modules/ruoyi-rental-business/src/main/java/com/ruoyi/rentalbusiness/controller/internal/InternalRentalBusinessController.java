package com.ruoyi.rentalbusiness.controller.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.rentalbusiness.model.OrderChannelCommand;
import com.ruoyi.rentalbusiness.model.OrderChannelVO;
import com.ruoyi.rentalbusiness.model.ProtocolCommand;
import com.ruoyi.rentalbusiness.model.ProtocolVO;
import com.ruoyi.rentalbusiness.service.OrderChannelService;
import com.ruoyi.rentalbusiness.service.ProtocolService;
import com.ruoyi.rentalbusiness.service.RentalBusinessService;
import com.ruoyi.rentalbusiness.service.PlatformOrderQueryService;
import com.ruoyi.rentalbusiness.service.PlatformAllianceOrderQueryService;
import com.ruoyi.rentalbusiness.service.PlatformAllianceDisputeQueryService;
import com.ruoyi.rentalbusiness.service.MerchantAllianceDisputeService;
import com.ruoyi.rentalbusiness.service.MerchantAllianceScopeService;
import com.ruoyi.rentalbusiness.service.MerchantOrderAllianceService;
import com.ruoyi.rentalbusiness.service.MerchantLogisticsTrackingService;
import com.ruoyi.rentalbusiness.task.RegionWeatherRefreshTask;
import com.ruoyi.rentalbusiness.task.InventoryForecastRefreshTask;
import com.ruoyi.rentalbusiness.task.InventoryAlertRefreshTask;
import com.ruoyi.rentalbusiness.task.OrderLifecycleRefreshTask;
import com.ruoyi.rentalbusiness.task.ExternalInventoryReservationExpiryTask;
import com.ruoyi.rentalbusiness.task.MerchantExternalOrderCallbackTask;
import com.ruoyi.system.api.model.PlatformOrderChannelCommand;
import com.ruoyi.system.api.model.PlatformOrderChannelView;
import com.ruoyi.system.api.model.PlatformProtocolCommand;
import com.ruoyi.system.api.model.PlatformProtocolView;
import com.ruoyi.system.api.model.PlatformWarehouseView;
import com.ruoyi.system.api.model.PlatformOrderPageView;
import com.ruoyi.system.api.model.PlatformOrderDetailView;
import com.ruoyi.system.api.model.PlatformAllianceOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderDetailView;
import com.ruoyi.system.api.model.PlatformAllianceDisputePageView;
import com.ruoyi.system.api.model.PlatformAllianceDisputeView;
import com.ruoyi.system.api.model.PlatformAllianceMediationCommand;
import com.ruoyi.system.api.model.PlatformAllianceSettlementSummaryView;
import com.ruoyi.system.api.model.PlatformAllianceScopeView;
import com.ruoyi.system.api.model.PlatformMerchantOverviewStat;
import com.ruoyi.system.api.model.PlatformMerchantBusinessView;
import com.ruoyi.system.api.model.PlatformMerchantGovernanceView;
import com.ruoyi.system.api.model.PlatformOperationsAnalyticsView;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchView;
import com.ruoyi.system.api.model.PlatformAfterSalesDetailView;
import com.ruoyi.system.api.model.PlatformAfterSalesEventCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesPageView;
import com.ruoyi.system.api.model.PlatformAfterSalesQuery;
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.api.model.PlatformLogisticsPageView;
import com.ruoyi.system.api.model.PlatformLogisticsQuery;
import com.ruoyi.rentalbusiness.service.PlatformMerchantOverviewQueryService;
import com.ruoyi.rentalbusiness.service.PlatformMerchantBusinessQueryService;
import com.ruoyi.rentalbusiness.service.PlatformOperationsAnalyticsService;
import com.ruoyi.rentalbusiness.service.PlatformExecutionWorkbenchService;
import com.ruoyi.rentalbusiness.service.MerchantFinanceSettlementService;
import com.ruoyi.merchant.api.model.MerchantFinanceSettlementView;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryQuery;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryUnitView;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentCompletionCommand;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentTarget;
import com.ruoyi.rentalbusiness.service.MerchantOrderPaymentService;
import com.ruoyi.rentalbusiness.service.PlatformAfterSalesService;
import com.ruoyi.rentalbusiness.service.PlatformLogisticsService;
import com.ruoyi.rentalbusiness.service.MerchantMarketInventoryService;

/**
 * 租赁业务服务内部契约接口。
 *
 * 该接口用于服务探活、跨服务校验和平台调度触发，不承载浏览器端业务能力。所有
 * 方法都必须通过内部请求头和 {@link InnerAuth} 校验后才能执行。
 */
@RestController
@RequestMapping("/internal/rental-business")
public class InternalRentalBusinessController
{
    private final RentalBusinessService rentalBusinessService;
    private final RegionWeatherRefreshTask regionWeatherRefreshTask;
    private final InventoryForecastRefreshTask inventoryForecastRefreshTask;
    private final InventoryAlertRefreshTask inventoryAlertRefreshTask;
    private final OrderLifecycleRefreshTask orderLifecycleRefreshTask;
    private final ExternalInventoryReservationExpiryTask externalInventoryReservationExpiryTask;
    private final MerchantExternalOrderCallbackTask merchantExternalOrderCallbackTask;
    private final OrderChannelService orderChannelService;
    private final ProtocolService protocolService;
    private final PlatformOrderQueryService platformOrderQueryService;
    private final PlatformMerchantOverviewQueryService platformMerchantOverviewQueryService;
    private final PlatformMerchantBusinessQueryService platformMerchantBusinessQueryService;
    private final PlatformOperationsAnalyticsService platformOperationsAnalyticsService;
    private final PlatformExecutionWorkbenchService platformExecutionWorkbenchService;
    private final PlatformAllianceOrderQueryService platformAllianceOrderQueryService;
    private final PlatformAllianceDisputeQueryService platformAllianceDisputeQueryService;
    private final PlatformAfterSalesService platformAfterSalesService;
    private final PlatformLogisticsService platformLogisticsService;
    private final MerchantAllianceDisputeService merchantAllianceDisputeService;
    private final MerchantAllianceScopeService merchantAllianceScopeService;
    private final MerchantOrderAllianceService merchantOrderAllianceService;
    private final MerchantLogisticsTrackingService merchantLogisticsTrackingService;
    private final MerchantFinanceSettlementService merchantFinanceSettlementService;
    private final MerchantOrderPaymentService merchantOrderPaymentService;
    private final MerchantMarketInventoryService merchantMarketInventoryService;

    /**
     * 注入租赁业务应用服务和天气刷新任务，确保内部控制器只编排应用能力，不直接操作 Mapper。
     *
     * @param rentalBusinessService 租赁业务应用服务
     * @param regionWeatherRefreshTask 市级天气刷新任务
     * @param orderChannelService 订单渠道应用服务
     * @param protocolService 协议版本应用服务
     */
    public InternalRentalBusinessController(RentalBusinessService rentalBusinessService,
            RegionWeatherRefreshTask regionWeatherRefreshTask, InventoryForecastRefreshTask inventoryForecastRefreshTask,
            InventoryAlertRefreshTask inventoryAlertRefreshTask,
            OrderLifecycleRefreshTask orderLifecycleRefreshTask,
            ExternalInventoryReservationExpiryTask externalInventoryReservationExpiryTask,
            MerchantExternalOrderCallbackTask merchantExternalOrderCallbackTask,
            OrderChannelService orderChannelService,
            ProtocolService protocolService, PlatformOrderQueryService platformOrderQueryService,
            PlatformMerchantOverviewQueryService platformMerchantOverviewQueryService,
            PlatformMerchantBusinessQueryService platformMerchantBusinessQueryService,
            PlatformOperationsAnalyticsService platformOperationsAnalyticsService,
            PlatformExecutionWorkbenchService platformExecutionWorkbenchService,
            PlatformAllianceOrderQueryService platformAllianceOrderQueryService,
            PlatformAllianceDisputeQueryService platformAllianceDisputeQueryService,
            PlatformAfterSalesService platformAfterSalesService,
            PlatformLogisticsService platformLogisticsService,
            MerchantAllianceDisputeService merchantAllianceDisputeService,
            MerchantAllianceScopeService merchantAllianceScopeService,
            MerchantOrderAllianceService merchantOrderAllianceService,
            MerchantLogisticsTrackingService merchantLogisticsTrackingService,
            MerchantFinanceSettlementService merchantFinanceSettlementService,
            MerchantOrderPaymentService merchantOrderPaymentService,
            MerchantMarketInventoryService merchantMarketInventoryService)
    {
        this.rentalBusinessService = rentalBusinessService;
        this.regionWeatherRefreshTask = regionWeatherRefreshTask;
        this.inventoryForecastRefreshTask = inventoryForecastRefreshTask;
        this.inventoryAlertRefreshTask = inventoryAlertRefreshTask;
        this.orderLifecycleRefreshTask = orderLifecycleRefreshTask;
        this.externalInventoryReservationExpiryTask = externalInventoryReservationExpiryTask;
        this.merchantExternalOrderCallbackTask = merchantExternalOrderCallbackTask;
        this.orderChannelService = orderChannelService;
        this.protocolService = protocolService;
        this.platformOrderQueryService = platformOrderQueryService;
        this.platformMerchantOverviewQueryService = platformMerchantOverviewQueryService;
        this.platformMerchantBusinessQueryService = platformMerchantBusinessQueryService;
        this.platformOperationsAnalyticsService = platformOperationsAnalyticsService;
        this.platformExecutionWorkbenchService = platformExecutionWorkbenchService;
        this.platformAllianceOrderQueryService = platformAllianceOrderQueryService;
        this.platformAllianceDisputeQueryService = platformAllianceDisputeQueryService;
        this.platformAfterSalesService = platformAfterSalesService;
        this.platformLogisticsService = platformLogisticsService;
        this.merchantAllianceDisputeService = merchantAllianceDisputeService;
        this.merchantAllianceScopeService = merchantAllianceScopeService;
        this.merchantOrderAllianceService = merchantOrderAllianceService;
        this.merchantLogisticsTrackingService = merchantLogisticsTrackingService;
        this.merchantFinanceSettlementService = merchantFinanceSettlementService;
        this.merchantOrderPaymentService = merchantOrderPaymentService;
        this.merchantMarketInventoryService = merchantMarketInventoryService;
    }

    /** 查询平台联盟成员共享范围，供平台治理页面展示。 */
    @InnerAuth
    @GetMapping("/platform/alliance-scopes")
    public R<List<PlatformAllianceScopeView>> listPlatformAllianceScopes(
            @RequestParam("tenantId") Long tenantId, @RequestParam("allianceId") Long allianceId,
            @RequestParam("memberId") Long memberId)
    {
        return R.ok(merchantAllianceScopeService.listForPlatform(tenantId, allianceId, memberId));
    }

    /**
     * 返回固定服务标识，供内部调用方确认请求已经到达 rental-business。
     *
     * @return 租赁业务服务标识
     */
    @InnerAuth
    @GetMapping("/ping")
    public R<String> ping()
    {
        return R.ok("rental-business");
    }

    /** 平台分页查看全部商户订单，商户筛选范围由平台服务解析后传入。 */
    @InnerAuth
    @GetMapping("/platform/orders")
    public R<PlatformOrderPageView> listPlatformOrders(
            @RequestParam(value = "memberIds", required = false) List<Long> memberIds,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "orderStatus", required = false) Integer orderStatus,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize)
    {
        return R.ok(platformOrderQueryService.list(memberIds, keyword, orderStatus, beginTime, endTime, pageNum,
                pageSize));
    }

    /** 返回指定订单详情及其批注、字段变更记录，供平台订单详情只读页面使用。 */
    @InnerAuth
    @GetMapping("/platform/orders/{orderId}")
    public R<PlatformOrderDetailView> getPlatformOrderDetail(@PathVariable("orderId") Long orderId)
    {
        return R.ok(platformOrderQueryService.detail(orderId));
    }

    /** 返回指定商户的经营统计，供平台服务组装管理视图。 */
    @InnerAuth
    @GetMapping("/platform/merchant-overview-stats")
    public R<List<PlatformMerchantOverviewStat>> listPlatformMerchantOverviewStats(
            @RequestParam("memberIds") List<Long> memberIds)
    {
        return R.ok(platformMerchantOverviewQueryService.list(memberIds));
    }

    /** 返回指定商户的仓库、商品、套餐和库存只读明细，供平台经营概况页面使用。 */
    @InnerAuth
    @GetMapping("/platform/merchant-business")
    public R<PlatformMerchantBusinessView> getPlatformMerchantBusiness(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId)
    {
        return R.ok(platformMerchantBusinessQueryService.get(tenantId, memberId));
    }

    /** 返回指定商户的履约、物流和风险治理汇总，供平台治理页面使用。 */
    @InnerAuth
    @GetMapping("/platform/merchant-governance")
    public R<PlatformMerchantGovernanceView> getPlatformMerchantGovernance(
            @RequestParam("tenantId") Long tenantId, @RequestParam("memberId") Long memberId)
    {
        return R.ok(platformMerchantBusinessQueryService.getGovernance(tenantId, memberId));
    }

    /** 查询平台经营分析的订单趋势和商户聚合指标。 */
    @InnerAuth
    @GetMapping("/platform/operations-analytics")
    public R<PlatformOperationsAnalyticsView> platformOperationsAnalytics(
            @RequestParam("memberIds") List<Long> memberIds,
            @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate)
    {
        PlatformOperationsAnalyticsService.OperationsResult source = platformOperationsAnalyticsService.query(memberIds,
                beginDate, endDate);
        PlatformOperationsAnalyticsView result = new PlatformOperationsAnalyticsView();
        result.setTrends(source.getTrends());
        result.setMerchantStats(source.getMerchantStats());
        result.setCityStats(source.getCityStats());
        result.setModelStats(source.getModelStats());
        return R.ok(result);
    }

    /** 查询订单履约、售后、物流和库存治理待办，供平台通用工作台聚合展示。 */
    @InnerAuth
    @GetMapping("/platform/execution-workbench")
    public R<PlatformExecutionWorkbenchView> getPlatformExecutionWorkbench()
    {
        return R.ok(platformExecutionWorkbenchService.overview());
    }

    /** 分页查询全部联盟协助订单，供平台端治理页面使用。 */
    @InnerAuth
    @GetMapping("/platform/alliance-orders")
    public R<PlatformAllianceOrderPageView> listPlatformAllianceOrders(
            @RequestParam(value = "allianceId", required = false) Long allianceId,
            @RequestParam(value = "sourceMemberId", required = false) Long sourceMemberId,
            @RequestParam(value = "targetMemberId", required = false) Long targetMemberId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "viewMode", required = false) Integer viewMode,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize)
    {
        com.ruoyi.system.api.model.PlatformAllianceOrderQuery query = new com.ruoyi.system.api.model.PlatformAllianceOrderQuery();
        query.setAllianceId(allianceId); query.setSourceMemberId(sourceMemberId); query.setTargetMemberId(targetMemberId);
        query.setStatus(status); query.setViewMode(viewMode); query.setBeginTime(beginTime); query.setEndTime(endTime);
        query.setPageNum(pageNum); query.setPageSize(pageSize);
        return R.ok(platformAllianceOrderQueryService.list(query));
    }

    /** 查询平台联盟订单详情、事件和结算记录。 */
    @InnerAuth
    @GetMapping("/platform/alliance-orders/{allianceOrderId}")
    public R<PlatformAllianceOrderDetailView> platformAllianceOrderDetail(
            @PathVariable Long allianceOrderId)
    {
        PlatformAllianceOrderDetailView detail = platformAllianceOrderQueryService.detail(allianceOrderId);
        return detail == null ? R.fail("联盟订单不存在") : R.ok(detail);
    }

    /** 查询联盟结算汇总，供平台端经营分析使用。 */
    @InnerAuth
    @GetMapping("/platform/alliance-settlements/summary")
    public R<PlatformAllianceSettlementSummaryView> platformAllianceSettlementSummary()
    {
        return R.ok(platformAllianceOrderQueryService.settlementSummary());
    }

    /** 由统一调度服务触发联盟子订单履约状态同步。 */
    @InnerAuth
    @PostMapping("/alliance-orders/synchronize-statuses")
    public R<Integer> synchronizeAllianceOrderStatuses()
    {
        return R.ok(merchantOrderAllianceService.synchronizeStatuses(null));
    }

    /** 由统一调度服务释放调解期限届满纠纷的冻结保证金。 */
    @InnerAuth
    @PostMapping("/alliance-disputes/release-expired")
    public R<Integer> releaseExpiredAllianceDisputes()
    {
        return R.ok(merchantAllianceDisputeService.releaseExpired());
    }

    /** 由统一调度服务刷新达到间隔且尚未签收的物流路线快照。 */
    @InnerAuth
    @PostMapping("/logistics/routes/refresh")
    public R<Integer> refreshLogisticsRoutes()
    {
        return R.ok(merchantLogisticsTrackingService.refreshUnreceivedRoutes());
    }

    /** 由统一调度服务触发所有商户未来三十天库存预测刷新。 */
    @InnerAuth
    @PostMapping("/inventory/forecast/refresh")
    public R<Integer> refreshInventoryForecast()
    {
        return R.ok(inventoryForecastRefreshTask.refreshNextThirtyDays());
    }

    /** 由统一调度服务触发所有商户库存告警事件刷新。 */
    @InnerAuth
    @PostMapping("/inventory/alerts/refresh")
    public R<Integer> refreshInventoryAlerts()
    {
        return R.ok(inventoryAlertRefreshTask.refresh());
    }

    /** 由统一调度服务释放已到期的外部库存预占。 */
    @InnerAuth
    @PostMapping("/inventory/reservations/expire")
    public R<Integer> expireExternalInventoryReservations()
    {
        return R.ok(externalInventoryReservationExpiryTask.expire());
    }

    /** 由统一调度服务发送外部正式订单回调出站事件。 */
    @InnerAuth
    @PostMapping("/external-orders/callbacks/dispatch")
    public R<Integer> dispatchExternalOrderCallbacks()
    {
        return R.ok(merchantExternalOrderCallbackTask.dispatch());
    }

    /** 由统一调度服务触发订单履约状态、订单明细和联盟原订单同步。 */
    @InnerAuth
    @PostMapping("/orders/lifecycle/refresh")
    public R<Integer> refreshOrderLifecycleStatuses()
    {
        return R.ok(orderLifecycleRefreshTask.refresh());
    }

    /** 查询指定商户参与的联盟结算记录，供商户财务中心使用。 */
    @InnerAuth
    @GetMapping("/merchant-finance/settlements")
    public R<List<MerchantFinanceSettlementView>> listMerchantFinanceSettlements(
            @RequestParam("tenantId") Long tenantId, @RequestParam("memberId") Long memberId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime)
    {
        return R.ok(merchantFinanceSettlementService.list(tenantId, memberId, status, beginTime, endTime));
    }

    /** 查询商户订单的支付目标，归属范围由租赁业务服务再次校验。 */
    @InnerAuth
    @GetMapping("/orders/payment-target")
    public R<MerchantOrderPaymentTarget> getMerchantOrderPaymentTarget(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam("orderId") Long orderId)
    {
        MerchantOrderPaymentTarget target = merchantOrderPaymentService.getTarget(tenantId, memberId, orderId);
        return target == null ? R.fail("订单不存在或无权支付") : R.ok(target);
    }

    /** 接收商户服务已经验签的订单支付结果，幂等更新订单付款状态。 */
    @InnerAuth
    @PostMapping("/orders/payment-complete")
    public R<Boolean> completeMerchantOrderPayment(@RequestBody MerchantOrderPaymentCompletionCommand command)
    {
        return merchantOrderPaymentService.complete(command) ? R.ok(true) : R.fail("订单付款状态更新失败");
    }

    /** 查询可用于周转市场上架的已认证逐件库存。 */
    @InnerAuth
    @PostMapping("/market/eligible-units")
    public R<List<MerchantMarketInventoryUnitView>> listMarketEligibleUnits(
            @RequestBody MerchantMarketInventoryQuery query)
    {
        return R.ok(merchantMarketInventoryService.eligible(query));
    }

    /** 分页查询所有联盟纠纷，供平台治理页面使用。 */
    @InnerAuth
    @GetMapping("/platform/alliance-disputes")
    public R<PlatformAllianceDisputePageView> listPlatformAllianceDisputes(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize)
    {
        return R.ok(platformAllianceDisputeQueryService.list(status, pageNum, pageSize));
    }

    /** 查询平台联盟纠纷详情。 */
    @InnerAuth
    @GetMapping("/platform/alliance-disputes/{disputeId}")
    public R<PlatformAllianceDisputeView> platformAllianceDisputeDetail(@PathVariable Long disputeId)
    {
        PlatformAllianceDisputeView detail = platformAllianceDisputeQueryService.detail(disputeId);
        return detail == null ? R.fail("联盟纠纷不存在") : R.ok(detail);
    }

    /** 平台提交联盟纠纷调解方案。 */
    @InnerAuth
    @PostMapping("/platform/alliance-disputes/mediation")
    public R<Void> proposePlatformAllianceMediation(@RequestBody PlatformAllianceMediationCommand command,
            @RequestParam("mediatorUserId") Long mediatorUserId)
    {
        merchantAllianceDisputeService.proposeMediation(command.getDisputeId(), command.getProposalAmount(),
                command.getProposalContent(), mediatorUserId);
        return R.ok();
    }

    /** 分页查询所有商户售后案件，供平台售后治理页面使用。 */
    @InnerAuth
    @GetMapping("/platform/after-sales")
    public R<PlatformAfterSalesPageView> listPlatformAfterSales(
            @RequestParam(value = "caseStatus", required = false) Integer caseStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "merchantKeyword", required = false) String merchantKeyword,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize)
    {
        PlatformAfterSalesQuery query = new PlatformAfterSalesQuery();
        query.setCaseStatus(caseStatus);
        query.setKeyword(keyword);
        query.setMerchantKeyword(merchantKeyword);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return R.ok(platformAfterSalesService.list(query));
    }

    /** 查询平台售后案件详情、处理事件和证据。 */
    @InnerAuth
    @GetMapping("/platform/after-sales/{caseId}")
    public R<PlatformAfterSalesDetailView> platformAfterSalesDetail(@PathVariable Long caseId)
    {
        return R.ok(platformAfterSalesService.detail(caseId));
    }

    /** 追加平台售后处理事件并推进案件状态。 */
    @InnerAuth
    @PostMapping("/platform/after-sales/{caseId}/events")
    public R<Void> appendPlatformAfterSalesEvent(@PathVariable Long caseId,
            @RequestBody PlatformAfterSalesEventCommand command,
            @RequestParam("operatorId") Long operatorId,
            @RequestParam("operatorName") String operatorName)
    {
        platformAfterSalesService.appendEvent(caseId, command, operatorId, operatorName);
        return R.ok();
    }

    /** 追加平台售后证据，并记录对应时间线事件。 */
    @InnerAuth
    @PostMapping("/platform/after-sales/{caseId}/evidence")
    public R<Void> appendPlatformAfterSalesEvidence(@PathVariable Long caseId,
            @RequestBody PlatformAfterSalesEvidenceCommand command,
            @RequestParam("operatorId") Long operatorId,
            @RequestParam("operatorName") String operatorName)
    {
        platformAfterSalesService.addEvidence(caseId, command, operatorId, operatorName);
        return R.ok();
    }

    /** 分页查询全部商户物流台账，供平台物流治理页面使用。 */
    @InnerAuth
    @GetMapping("/platform/logistics")
    public R<PlatformLogisticsPageView> listPlatformLogistics(PlatformLogisticsQuery query)
    {
        return R.ok(platformLogisticsService.list(query));
    }

    /** 查询平台物流记录及已经保存的路线节点。 */
    @InnerAuth
    @GetMapping("/platform/logistics/{logisticsId}")
    public R<PlatformLogisticsDetailView> getPlatformLogisticsDetail(@PathVariable Long logisticsId)
    {
        return R.ok(platformLogisticsService.detail(logisticsId));
    }

    /** 手工刷新平台选中的单条物流路线，不改变订单履约状态。 */
    @InnerAuth
    @PostMapping("/platform/logistics/{logisticsId}/refresh")
    public R<Integer> refreshPlatformLogistics(@PathVariable Long logisticsId)
    {
        return R.ok(platformLogisticsService.refresh(logisticsId));
    }

    /**
     * 查询指定商户的仓库内部视图，平台服务和商户服务共用同一份业务库事实。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @param status 仓库状态，为空时不限制状态
     * @return 仓库内部传输对象集合
     */
    @InnerAuth
    @GetMapping("/warehouses")
    public R<List<PlatformWarehouseView>> warehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam(value = "status", required = false) Integer status)
    {
        return R.ok(toWarehouseViews(rentalBusinessService.listWarehousesForInternal(tenantId, memberId, status)));
    }

    /**
     * 统计指定商户启用仓库中命中的仓库唯一编号数量，供平台端校验仓库权限提交内容。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @param warehouseIds 待校验仓库唯一编号集合
     * @return 命中的启用仓库数量
     */
    @InnerAuth
    @GetMapping("/warehouses/count")
    public R<Integer> countWarehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam("warehouseIds") List<Long> warehouseIds)
    {
        return R.ok(rentalBusinessService.countEnabledWarehousesForInternal(tenantId, memberId, warehouseIds));
    }

    /**
     * 判断标准型号是否被租赁业务库中的商品引用。
     *
     * @param modelId 平台标准型号唯一编号
     * @return 已引用返回 true，否则返回 false
     */
    @InnerAuth
    @GetMapping("/models/{modelId}/references")
    public R<Boolean> modelReferences(@PathVariable("modelId") Long modelId)
    {
        return R.ok(rentalBusinessService.hasModelReferencesForInternal(modelId));
    }

    /**
     * 由平台定时任务触发全部市级行政区三日天气刷新。
     *
     * <p>该方法直接调用与业务服务任务相同的 Spring Bean，天气任务内部负责上海时区、
     * Redis 分布式锁、城市请求去重、三日数据写入和异常记录；平台端不会绕过业务服务直接访问
     * {@code ry_business} 数据库。</p>
     *
     * @return 任务调用完成结果；任务内部的单城市失败会记录失败天气快照并继续处理
     */
    @InnerAuth
    @PostMapping("/weather/refresh")
    public R<Void> refreshThreeDayWeather()
    {
        regionWeatherRefreshTask.refreshThreeDayWeather();
        return R.ok();
    }

    /** 查询平台联盟订单渠道，供 platform-service 的平台公开接口调用。 */
    @InnerAuth
    @GetMapping("/platform/order-channels")
    public R<List<PlatformOrderChannelView>> listPlatformOrderChannels(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status)
    {
        return R.ok(toPlatformChannelViews(orderChannelService.listPlatformChannels(keyword, status)));
    }

    /** 新增平台联盟订单渠道，平台身份由上游请求头传递并由业务服务再次校验。 */
    @InnerAuth
    @PostMapping("/platform/order-channels")
    public R<PlatformOrderChannelView> addPlatformOrderChannel(@RequestBody PlatformOrderChannelCommand command)
    {
        return R.ok(toPlatformChannelView(orderChannelService.addChannel(toOrderChannelCommand(command))));
    }

    /** 修改平台联盟订单渠道。 */
    @InnerAuth
    @PutMapping("/platform/order-channels")
    public R<PlatformOrderChannelView> updatePlatformOrderChannel(@RequestBody PlatformOrderChannelCommand command)
    {
        return R.ok(toPlatformChannelView(orderChannelService.updatePlatformChannel(toOrderChannelCommand(command))));
    }

    /** 软删除平台联盟订单渠道。 */
    @InnerAuth
    @DeleteMapping("/platform/order-channels/{channelId}")
    public R<Boolean> deletePlatformOrderChannel(@PathVariable Long channelId)
    {
        return R.ok(Boolean.valueOf(orderChannelService.deletePlatformChannel(channelId) > 0));
    }

    /** 查询平台协议版本。 */
    @InnerAuth
    @GetMapping("/platform/protocols")
    public R<List<PlatformProtocolView>> listPlatformProtocols(
            @RequestParam(value = "protocolKey", required = false) String protocolKey)
    {
        return R.ok(toPlatformProtocolViews(protocolService.listPlatform(protocolKey)));
    }

    /** 新增平台协议草稿。 */
    @InnerAuth
    @PostMapping("/platform/protocols")
    public R<PlatformProtocolView> addPlatformProtocol(@RequestBody PlatformProtocolCommand command)
    {
        return R.ok(toPlatformProtocolView(protocolService.add(toProtocolCommand(command))));
    }

    /** 修改平台协议草稿。 */
    @InnerAuth
    @PutMapping("/platform/protocols")
    public R<PlatformProtocolView> updatePlatformProtocol(@RequestBody PlatformProtocolCommand command)
    {
        return R.ok(toPlatformProtocolView(protocolService.update(toProtocolCommand(command))));
    }

    /** 发布平台协议版本。 */
    @InnerAuth
    @PostMapping("/platform/protocols/{protocolId}/publish")
    public R<PlatformProtocolView> publishPlatformProtocol(@PathVariable Long protocolId,
            @RequestParam(value = "effectiveTime", required = false) String effectiveTime)
    {
        return R.ok(toPlatformProtocolView(protocolService.publish(protocolId, effectiveTime)));
    }

    /**
     * 将租赁业务 Mapper 的动态结果转换为平台内部契约对象，防止数据库 Map 越过服务边界。
     *
     * @param rows 租赁业务仓库查询结果
     * @return 平台内部仓库传输对象集合
     */
    private List<PlatformWarehouseView> toWarehouseViews(List<Map<String, Object>> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformWarehouseView> result = new ArrayList<PlatformWarehouseView>(rows.size());
        for (Map<String, Object> row : rows)
        {
            PlatformWarehouseView view = new PlatformWarehouseView();
            view.setWarehouseId(longValue(row.get("warehouseId")));
            view.setTenantId(longValue(row.get("tenantId")));
            view.setMemberId(longValue(row.get("memberId")));
            view.setWarehouseCode(stringValue(row.get("warehouseCode")));
            view.setWarehouseName(stringValue(row.get("warehouseName")));
            view.setStatus(integerValue(row.get("status")));
            result.add(view);
        }
        return result;
    }

    /** 将 JDBC 数值字段统一转换为长整数。 */
    private Long longValue(Object value)
    {
        return value == null ? null : value instanceof Number ? ((Number) value).longValue()
                : Long.valueOf(String.valueOf(value));
    }

    /** 将 JDBC 字段统一转换为文本。 */
    private String stringValue(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    /** 将 JDBC 字段统一转换为整数状态编码。 */
    private Integer integerValue(Object value)
    {
        return value == null ? null : value instanceof Number ? ((Number) value).intValue()
                : Integer.valueOf(String.valueOf(value));
    }

    /** 将平台 API 契约命令转换成租赁业务内部命令。 */
    private OrderChannelCommand toOrderChannelCommand(PlatformOrderChannelCommand source)
    {
        OrderChannelCommand target = new OrderChannelCommand();
        if (source != null)
        {
            target.channelId = source.getChannelId();
            target.channelName = source.getChannelName();
            target.channelType = source.getChannelType();
            target.status = source.getStatus();
            target.sortNo = source.getSortNo();
            target.reason = source.getReason();
        }
        return target;
    }

    /** 将平台 API 契约命令转换成租赁业务内部协议命令。 */
    private ProtocolCommand toProtocolCommand(PlatformProtocolCommand source)
    {
        ProtocolCommand target = new ProtocolCommand();
        if (source != null)
        {
            target.protocolId = source.getProtocolId();
            target.protocolKey = source.getProtocolKey();
            target.protocolTitle = source.getProtocolTitle();
            target.protocolContent = source.getProtocolContent();
            target.scopeTypes = source.getScopeTypes();
            target.remark = source.getRemark();
        }
        return target;
    }

    /** 将租赁业务订单渠道视图转换为平台内部契约视图。 */
    private List<PlatformOrderChannelView> toPlatformChannelViews(List<OrderChannelVO> sources)
    {
        List<PlatformOrderChannelView> result = new ArrayList<PlatformOrderChannelView>();
        if (sources != null)
        {
            for (OrderChannelVO source : sources)
            {
                result.add(toPlatformChannelView(source));
            }
        }
        return result;
    }

    /** 转换单个订单渠道视图。 */
    private PlatformOrderChannelView toPlatformChannelView(OrderChannelVO source)
    {
        PlatformOrderChannelView target = new PlatformOrderChannelView();
        if (source != null)
        {
            target.setChannelId(source.channelId);
            target.setChannelCode(source.channelCode);
            target.setChannelName(source.channelName);
            target.setChannelType(source.channelType);
            target.setScopeType(source.scopeType);
            target.setStatus(source.status);
            target.setSortNo(source.sortNo);
        }
        return target;
    }

    /** 将租赁业务协议视图转换为平台内部契约视图。 */
    private List<PlatformProtocolView> toPlatformProtocolViews(List<ProtocolVO> sources)
    {
        List<PlatformProtocolView> result = new ArrayList<PlatformProtocolView>();
        if (sources != null)
        {
            for (ProtocolVO source : sources)
            {
                result.add(toPlatformProtocolView(source));
            }
        }
        return result;
    }

    /** 转换单个协议视图。 */
    private PlatformProtocolView toPlatformProtocolView(ProtocolVO source)
    {
        PlatformProtocolView target = new PlatformProtocolView();
        if (source != null)
        {
            target.setProtocolId(source.protocolId);
            target.setProtocolKey(source.protocolKey);
            target.setVersionNo(source.versionNo);
            target.setProtocolTitle(source.protocolTitle);
            target.setProtocolContent(source.protocolContent);
            target.setContentHash(source.contentHash);
            target.setProtocolStatus(source.protocolStatus);
            target.setEffectiveTime(source.effectiveTime);
            target.setScopeTypes(source.scopeTypes);
            target.setScopeLabels(source.scopeLabels);
        }
        return target;
    }
}
