package com.ruoyi.system.api.factory;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;
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
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.api.model.PlatformLogisticsPageView;

/** 租赁业务服务内部契约降级处理。 */
@Component
public class RemoteRentalBusinessFallbackFactory implements FallbackFactory<RemoteRentalBusinessService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteRentalBusinessFallbackFactory.class);

    /**
     * 创建失败即拒绝的降级实现，避免租赁业务服务不可用时平台误放行仓库范围或删除标准型号。
     *
     * @param cause 远程调用失败原因
     * @return 明确返回失败结果的降级实现
     */
    @Override
    public RemoteRentalBusinessService create(Throwable cause)
    {
        log.error("租赁业务服务内部调用失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemoteRentalBusinessService()
        {
            @Override
            public R<List<PlatformAllianceScopeView>> listPlatformAllianceScopes(Long tenantId, Long allianceId,
                    Long memberId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }
            /** 远程仓库查询失败时返回失败结果，禁止调用方使用不完整列表。 */
            @Override
            public R<List<PlatformWarehouseView>> listWarehouses(Long tenantId, Long memberId, Integer status,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 远程仓库范围校验失败时返回失败结果，禁止调用方放行越权仓库。 */
            @Override
            public R<Integer> countEnabledWarehouses(Long tenantId, Long memberId, List<Long> warehouseIds,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 远程型号引用校验失败时返回失败结果，禁止调用方删除标准型号。 */
            @Override
            public R<Boolean> hasModelReferences(Long modelId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台订单查询失败时拒绝返回不完整分页数据。 */
            @Override
            public R<PlatformOrderPageView> listPlatformOrders(List<Long> memberIds, String keyword,
                    Integer orderStatus, String beginTime, String endTime, Integer pageNum, Integer pageSize,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台订单详情查询失败时拒绝返回不完整订单信息。 */
            @Override
            public R<PlatformOrderDetailView> getPlatformOrderDetail(Long orderId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 商户经营统计查询失败时返回失败结果，禁止平台把不完整数据当成真实统计。 */
            @Override
            public R<List<PlatformMerchantOverviewStat>> listPlatformMerchantOverviewStats(List<Long> memberIds,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 商户经营明细查询失败时拒绝返回不完整数据。 */
            @Override
            public R<PlatformMerchantBusinessView> getPlatformMerchantBusiness(Long tenantId, Long memberId,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 商户治理汇总查询失败时拒绝返回不完整数据。 */
            @Override
            public R<PlatformMerchantGovernanceView> getPlatformMerchantGovernance(Long tenantId, Long memberId,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台经营分析查询失败时拒绝返回不完整指标。 */
            @Override
            public R<PlatformOperationsAnalyticsView> getPlatformOperationsAnalytics(List<Long> memberIds,
                    String beginDate, String endDate, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台执行面待办查询失败时返回明确失败结果，避免平台误报待办已清零。 */
            @Override
            public R<PlatformExecutionWorkbenchView> getPlatformExecutionWorkbench(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformAllianceOrderPageView> listPlatformAllianceOrders(Long allianceId,
                    Long sourceMemberId, Long targetMemberId, Integer status, Integer viewMode, String beginTime, String endTime,
                    Integer pageNum, Integer pageSize, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformAllianceOrderDetailView> getPlatformAllianceOrderDetail(Long allianceOrderId,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformAllianceSettlementSummaryView> getPlatformAllianceSettlementSummary(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Integer> synchronizeAllianceOrderStatuses(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Integer> releaseExpiredAllianceDisputes(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Integer> refreshLogisticsRoutes(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 库存预测刷新失败时返回失败结果，禁止调度平台误记为成功。 */
            @Override
            public R<Integer> refreshInventoryForecast(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Integer> refreshInventoryAlerts(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 外部库存预占过期释放失败时返回失败结果，禁止调度平台误记为成功。 */
            @Override
            public R<Integer> expireExternalInventoryReservations(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 外部订单回调发送失败时返回失败结果，避免调度任务误判为已送达。 */
            @Override
            public R<Integer> dispatchExternalOrderCallbacks(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Integer> refreshOrderLifecycleStatuses(String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformAllianceDisputePageView> listPlatformAllianceDisputes(Integer status, Integer pageNum,
                    Integer pageSize, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformAllianceDisputeView> getPlatformAllianceDisputeDetail(Long disputeId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Void> proposePlatformAllianceMediation(PlatformAllianceMediationCommand command,
                    Long mediatorUserId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformAfterSalesPageView> listPlatformAfterSales(Integer caseStatus, String keyword,
                    String merchantKeyword, Integer pageNum, Integer pageSize, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformAfterSalesDetailView> getPlatformAfterSalesDetail(Long caseId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Void> appendPlatformAfterSalesEvent(Long caseId, PlatformAfterSalesEventCommand command,
                    Long operatorId, String operatorName, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Void> appendPlatformAfterSalesEvidence(Long caseId,
                    PlatformAfterSalesEvidenceCommand command, Long operatorId, String operatorName, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformLogisticsPageView> listPlatformLogistics(String keyword, String merchantKeyword,
                    Integer direction, Integer routeStatus, Boolean abnormalOnly, Integer pageNum, Integer pageSize,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<PlatformLogisticsDetailView> getPlatformLogisticsDetail(Long logisticsId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Integer> refreshPlatformLogistics(Long logisticsId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台渠道查询失败时拒绝返回不完整数据。 */
            @Override
            public R<List<PlatformOrderChannelView>> listPlatformOrderChannels(String keyword, Integer status,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台渠道新增失败时拒绝误报成功。 */
            @Override
            public R<PlatformOrderChannelView> addPlatformOrderChannel(PlatformOrderChannelCommand command,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台渠道修改失败时拒绝误报成功。 */
            @Override
            public R<PlatformOrderChannelView> updatePlatformOrderChannel(PlatformOrderChannelCommand command,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台渠道删除失败时拒绝误报成功。 */
            @Override
            public R<Boolean> deletePlatformOrderChannel(Long channelId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台协议查询失败时拒绝返回不完整数据。 */
            @Override
            public R<List<PlatformProtocolView>> listPlatformProtocols(String protocolKey, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台协议新增失败时拒绝误报成功。 */
            @Override
            public R<PlatformProtocolView> addPlatformProtocol(PlatformProtocolCommand command, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台协议修改失败时拒绝误报成功。 */
            @Override
            public R<PlatformProtocolView> updatePlatformProtocol(PlatformProtocolCommand command, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 平台协议发布失败时拒绝误报成功。 */
            @Override
            public R<PlatformProtocolView> publishPlatformProtocol(Long protocolId, String effectiveTime,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }
        };
    }
}
