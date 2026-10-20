package com.ruoyi.system.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteRentalBusinessFallbackFactory;
import com.ruoyi.system.api.model.PlatformOrderChannelCommand;
import com.ruoyi.system.api.model.PlatformOrderChannelView;
import com.ruoyi.system.api.model.PlatformProtocolCommand;
import com.ruoyi.system.api.model.PlatformProtocolView;
import com.ruoyi.system.api.model.PlatformWarehouseView;
import com.ruoyi.system.api.model.PlatformOrderPageView;
import com.ruoyi.system.api.model.PlatformOrderDetailView;
import com.ruoyi.system.api.model.PlatformAllianceOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderQuery;
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
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.api.model.PlatformLogisticsPageView;

/**
 * 平台服务访问租赁业务服务的内部契约。
 *
 * <p>平台服务不得直接访问 {@code ry_business} 数据库，仓库范围和业务引用校验
 * 必须通过该契约进入 rental-business。</p>
 */
@FeignClient(contextId = "remoteRentalBusinessService", value = ServiceNameConstants.RENTAL_BUSINESS_SERVICE,
        fallbackFactory = RemoteRentalBusinessFallbackFactory.class)
public interface RemoteRentalBusinessService
{
    /** 租赁业务服务内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/rental-business";

    /** 查询指定联盟成员的共享范围。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/alliance-scopes")
    R<List<PlatformAllianceScopeView>> listPlatformAllianceScopes(
            @RequestParam("tenantId") Long tenantId, @RequestParam("allianceId") Long allianceId,
            @RequestParam("memberId") Long memberId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 查询指定商户的仓库列表。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @param status 仓库状态，为空时不限制状态
     * @param source 内部服务调用标识
     * @return 仓库传输对象集合
     */
    @GetMapping(INTERNAL_PREFIX + "/warehouses")
    R<List<PlatformWarehouseView>> listWarehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 统计指定商户的启用仓库中命中给定唯一编号集合的数量。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @param warehouseIds 待校验仓库唯一编号集合
     * @param source 内部服务调用标识
     * @return 命中的启用仓库数量
     */
    @GetMapping(INTERNAL_PREFIX + "/warehouses/count")
    R<Integer> countEnabledWarehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam("warehouseIds") List<Long> warehouseIds,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 判断标准型号是否已经被租赁业务数据引用。
     *
     * @param modelId 平台标准型号唯一编号
     * @param source 内部服务调用标识
     * @return 已引用返回 true，否则返回 false
     */
    @GetMapping(INTERNAL_PREFIX + "/models/{modelId}/references")
    R<Boolean> hasModelReferences(@PathVariable("modelId") Long modelId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 分页查询全部商户订单，可按商户、关键词、状态和创建时间限制范围。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/orders")
    R<PlatformOrderPageView> listPlatformOrders(
            @RequestParam(value = "memberIds", required = false) List<Long> memberIds,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "orderStatus", required = false) Integer orderStatus,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询指定订单详情及订单处理记录。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/orders/{orderId}")
    R<PlatformOrderDetailView> getPlatformOrderDetail(@PathVariable("orderId") Long orderId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询指定商户的经营统计，返回结果仅供平台服务内部组装。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/merchant-overview-stats")
    R<List<PlatformMerchantOverviewStat>> listPlatformMerchantOverviewStats(
            @RequestParam("memberIds") List<Long> memberIds,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询指定商户的仓库、商品、套餐和库存只读明细。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/merchant-business")
    R<PlatformMerchantBusinessView> getPlatformMerchantBusiness(
            @RequestParam("tenantId") Long tenantId, @RequestParam("memberId") Long memberId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询指定商户的履约、物流和风险治理汇总。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/merchant-governance")
    R<PlatformMerchantGovernanceView> getPlatformMerchantGovernance(
            @RequestParam("tenantId") Long tenantId, @RequestParam("memberId") Long memberId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台经营分析的订单趋势和商户聚合指标。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/operations-analytics")
    R<PlatformOperationsAnalyticsView> getPlatformOperationsAnalytics(
            @RequestParam("memberIds") List<Long> memberIds, @RequestParam("beginDate") String beginDate,
            @RequestParam("endDate") String endDate, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台执行面待办汇总。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/execution-workbench")
    R<PlatformExecutionWorkbenchView> getPlatformExecutionWorkbench(
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台联盟订单治理分页数据。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/alliance-orders")
    R<PlatformAllianceOrderPageView> listPlatformAllianceOrders(
            @RequestParam(value = "allianceId", required = false) Long allianceId,
            @RequestParam(value = "sourceMemberId", required = false) Long sourceMemberId,
            @RequestParam(value = "targetMemberId", required = false) Long targetMemberId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "viewMode", required = false) Integer viewMode,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台联盟订单详情、事件和结算记录。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/alliance-orders/{allianceOrderId}")
    R<PlatformAllianceOrderDetailView> getPlatformAllianceOrderDetail(
            @PathVariable("allianceOrderId") Long allianceOrderId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台联盟结算汇总。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/alliance-settlements/summary")
    R<PlatformAllianceSettlementSummaryView> getPlatformAllianceSettlementSummary(
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 同步所有租户已创建子订单的联盟履约状态。 */
    @PostMapping(INTERNAL_PREFIX + "/alliance-orders/synchronize-statuses")
    R<Integer> synchronizeAllianceOrderStatuses(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 释放调解期限届满纠纷的冻结保证金。 */
    @PostMapping(INTERNAL_PREFIX + "/alliance-disputes/release-expired")
    R<Integer> releaseExpiredAllianceDisputes(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 刷新达到间隔且尚未签收的物流路线快照。 */
    @PostMapping(INTERNAL_PREFIX + "/logistics/routes/refresh")
    R<Integer> refreshLogisticsRoutes(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 刷新所有商户未来三十天库存预测快照。 */
    @PostMapping(INTERNAL_PREFIX + "/inventory/forecast/refresh")
    R<Integer> refreshInventoryForecast(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 刷新所有商户库存告警派生事件。 */
    @PostMapping(INTERNAL_PREFIX + "/inventory/alerts/refresh")
    R<Integer> refreshInventoryAlerts(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 释放已到期的外部库存预占。 */
    @PostMapping(INTERNAL_PREFIX + "/inventory/reservations/expire")
    R<Integer> expireExternalInventoryReservations(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 发送外部正式订单回调出站事件。 */
    @PostMapping(INTERNAL_PREFIX + "/external-orders/callbacks/dispatch")
    R<Integer> dispatchExternalOrderCallbacks(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 刷新达到履约时间条件的订单状态及关联数据。 */
    @PostMapping(INTERNAL_PREFIX + "/orders/lifecycle/refresh")
    R<Integer> refreshOrderLifecycleStatuses(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台联盟纠纷治理分页数据。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/alliance-disputes")
    R<PlatformAllianceDisputePageView> listPlatformAllianceDisputes(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台联盟纠纷详情。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/alliance-disputes/{disputeId}")
    R<PlatformAllianceDisputeView> getPlatformAllianceDisputeDetail(
            @PathVariable("disputeId") Long disputeId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 平台提交联盟纠纷调解方案。 */
    @PostMapping(INTERNAL_PREFIX + "/platform/alliance-disputes/mediation")
    R<Void> proposePlatformAllianceMediation(@RequestBody PlatformAllianceMediationCommand command,
            @RequestParam("mediatorUserId") Long mediatorUserId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台售后案件。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/after-sales")
    R<PlatformAfterSalesPageView> listPlatformAfterSales(
            @RequestParam(value = "caseStatus", required = false) Integer caseStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "merchantKeyword", required = false) String merchantKeyword,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台售后案件详情、事件和证据。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/after-sales/{caseId}")
    R<PlatformAfterSalesDetailView> getPlatformAfterSalesDetail(@PathVariable("caseId") Long caseId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 追加平台售后处理事件。 */
    @PostMapping(INTERNAL_PREFIX + "/platform/after-sales/{caseId}/events")
    R<Void> appendPlatformAfterSalesEvent(@PathVariable("caseId") Long caseId,
            @RequestBody PlatformAfterSalesEventCommand command, @RequestParam("operatorId") Long operatorId,
            @RequestParam("operatorName") String operatorName,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 追加平台售后证据。 */
    @PostMapping(INTERNAL_PREFIX + "/platform/after-sales/{caseId}/evidence")
    R<Void> appendPlatformAfterSalesEvidence(@PathVariable("caseId") Long caseId,
            @RequestBody PlatformAfterSalesEvidenceCommand command, @RequestParam("operatorId") Long operatorId,
            @RequestParam("operatorName") String operatorName,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台物流台账。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/logistics")
    R<PlatformLogisticsPageView> listPlatformLogistics(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "merchantKeyword", required = false) String merchantKeyword,
            @RequestParam(value = "direction", required = false) Integer direction,
            @RequestParam(value = "routeStatus", required = false) Integer routeStatus,
            @RequestParam(value = "abnormalOnly", required = false) Boolean abnormalOnly,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台物流详情和已缓存路线。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/logistics/{logisticsId}")
    R<PlatformLogisticsDetailView> getPlatformLogisticsDetail(@PathVariable("logisticsId") Long logisticsId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 手工刷新平台选中的单条物流路线，不改变订单履约状态。 */
    @PostMapping(INTERNAL_PREFIX + "/platform/logistics/{logisticsId}/refresh")
    R<Integer> refreshPlatformLogistics(@PathVariable("logisticsId") Long logisticsId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台联盟订单渠道。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/order-channels")
    R<List<PlatformOrderChannelView>> listPlatformOrderChannels(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 新增平台联盟订单渠道。 */
    @PostMapping(INTERNAL_PREFIX + "/platform/order-channels")
    R<PlatformOrderChannelView> addPlatformOrderChannel(@RequestBody PlatformOrderChannelCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 修改平台联盟订单渠道。 */
    @PutMapping(INTERNAL_PREFIX + "/platform/order-channels")
    R<PlatformOrderChannelView> updatePlatformOrderChannel(@RequestBody PlatformOrderChannelCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 软删除平台联盟订单渠道。 */
    @DeleteMapping(INTERNAL_PREFIX + "/platform/order-channels/{channelId}")
    R<Boolean> deletePlatformOrderChannel(@PathVariable("channelId") Long channelId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询平台协议版本。 */
    @GetMapping(INTERNAL_PREFIX + "/platform/protocols")
    R<List<PlatformProtocolView>> listPlatformProtocols(
            @RequestParam(value = "protocolKey", required = false) String protocolKey,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 新增平台协议草稿。 */
    @PostMapping(INTERNAL_PREFIX + "/platform/protocols")
    R<PlatformProtocolView> addPlatformProtocol(@RequestBody PlatformProtocolCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 修改平台协议草稿。 */
    @PutMapping(INTERNAL_PREFIX + "/platform/protocols")
    R<PlatformProtocolView> updatePlatformProtocol(@RequestBody PlatformProtocolCommand command,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 发布平台协议版本。 */
    @PostMapping(INTERNAL_PREFIX + "/platform/protocols/{protocolId}/publish")
    R<PlatformProtocolView> publishPlatformProtocol(@PathVariable("protocolId") Long protocolId,
            @RequestParam(value = "effectiveTime", required = false) String effectiveTime,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
