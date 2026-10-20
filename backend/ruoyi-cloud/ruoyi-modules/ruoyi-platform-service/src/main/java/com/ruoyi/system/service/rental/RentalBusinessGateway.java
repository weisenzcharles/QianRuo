package com.ruoyi.system.service.rental;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.api.RemoteRentalBusinessService;
import com.ruoyi.merchant.api.RemoteMerchantAllianceService;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.model.PlatformOrderChannelCommand;
import com.ruoyi.system.api.model.PlatformOrderChannelView;
import com.ruoyi.system.api.model.PlatformProtocolCommand;
import com.ruoyi.system.api.model.PlatformProtocolView;
import com.ruoyi.system.api.model.PlatformWarehouseView;
import com.ruoyi.system.api.model.PlatformOrderPageView;
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
import com.ruoyi.system.model.rental.PlatformOrderChannelVO;
import com.ruoyi.system.model.rental.PlatformProtocolVO;

/**
 * 平台服务访问租赁业务服务的应用网关。
 *
 * <p>平台控制器只依赖该网关，不直接依赖租赁业务 Mapper 或 {@code ry_business} 数据库。
 * 网关对远程失败统一抛出业务异常，避免把降级结果误当作空数据继续执行。</p>
 */
@Service
public class RentalBusinessGateway
{
    private final RemoteRentalBusinessService remoteRentalBusinessService;
    private final RemoteMerchantAllianceService remoteMerchantAllianceService;

    /** 注入租赁业务服务内部 Feign 契约。 */
    @Autowired
    public RentalBusinessGateway(RemoteRentalBusinessService remoteRentalBusinessService,
            RemoteMerchantAllianceService remoteMerchantAllianceService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
        this.remoteMerchantAllianceService = remoteMerchantAllianceService;
    }

    /** 保留纯租赁业务测试和旧调用方的构造方式。 */
    public RentalBusinessGateway(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this(remoteRentalBusinessService, null);
    }

    /** 查询平台联盟管理分页数据。 */
    public TableDataInfo listMerchantAlliances(String keyword, Integer status, Integer pageNum, Integer pageSize)
    {
        R<TableDataInfo> response = remoteMerchantAllianceService.listForPlatform(keyword, status, pageNum, pageSize,
                SecurityConstants.INNER, "platform");
        return require(response, "查询联盟管理");
    }

    /** 查询平台联盟成员。 */
    public List<MerchantAllianceOption> listMerchantAllianceMembers(Long allianceId)
    {
        R<List<MerchantAllianceOption>> response = remoteMerchantAllianceService.membersForPlatform(allianceId,
                SecurityConstants.INNER, "platform");
        List<MerchantAllianceOption> data = require(response, "查询联盟成员");
        return data == null ? Collections.emptyList() : data;
    }

    /** 查询联盟成员共享仓库和商品范围。 */
    public List<PlatformAllianceScopeView> listMerchantAllianceScopes(Long tenantId, Long allianceId, Long memberId)
    {
        R<List<PlatformAllianceScopeView>> response = remoteRentalBusinessService.listPlatformAllianceScopes(
                tenantId, allianceId, memberId, SecurityConstants.INNER);
        List<PlatformAllianceScopeView> data = require(response, "查询联盟共享范围");
        return data == null ? Collections.emptyList() : data;
    }

    /** 更新联盟或成员状态。 */
    public void updateMerchantAllianceStatus(Long allianceId, Long memberId, Integer status)
    {
        R<Void> response = remoteMerchantAllianceService.updateStatusForPlatform(allianceId, memberId, status,
                SecurityConstants.INNER, "platform");
        require(response, "更新联盟状态");
    }

    /**
     * 查询指定商户的启用仓库，供平台商户用户编辑和仓库权限校验使用。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @return 启用仓库集合
     */
    public List<PlatformWarehouseView> listEnabledWarehouses(Long tenantId, Long memberId)
    {
        R<List<PlatformWarehouseView>> response = remoteRentalBusinessService.listWarehouses(tenantId, memberId,
                Integer.valueOf(0), SecurityConstants.INNER);
        List<PlatformWarehouseView> data = require(response, "查询商户仓库");
        return data == null ? Collections.emptyList() : data;
    }

    /**
     * 校验仓库唯一编号集合是否全部属于指定商户的启用仓库。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @param warehouseIds 待校验仓库唯一编号集合
     * @return 命中的启用仓库数量
     */
    public int countEnabledWarehouses(Long tenantId, Long memberId, List<Long> warehouseIds)
    {
        if (warehouseIds == null || warehouseIds.isEmpty())
        {
            return 0;
        }
        R<Integer> response = remoteRentalBusinessService.countEnabledWarehouses(tenantId, memberId, warehouseIds,
                SecurityConstants.INNER);
        Integer data = require(response, "校验商户仓库范围");
        if (data == null)
        {
            throw new ServiceException("校验商户仓库范围返回空结果");
        }
        return data.intValue();
    }

    /**
     * 判断标准型号是否被租赁业务数据引用，供平台删除标准型号前执行保护校验。
     *
     * @param modelId 平台标准型号唯一编号
     * @return 已被业务引用返回 true
     */
    public boolean hasModelReferences(Long modelId)
    {
        R<Boolean> response = remoteRentalBusinessService.hasModelReferences(modelId, SecurityConstants.INNER);
        Boolean data = require(response, "校验标准型号业务引用");
        if (data == null)
        {
            throw new ServiceException("校验标准型号业务引用返回空结果");
        }
        return data.booleanValue();
    }

    /** 分页查询平台可见的商户订单。 */
    public PlatformOrderPageView listPlatformOrders(List<Long> memberIds, String keyword, Integer orderStatus,
            String beginTime, String endTime, Integer pageNum, Integer pageSize)
    {
        R<PlatformOrderPageView> response = remoteRentalBusinessService.listPlatformOrders(memberIds, keyword,
                orderStatus, beginTime, endTime, pageNum, pageSize, SecurityConstants.INNER);
        PlatformOrderPageView data = require(response, "查询平台订单");
        if (data == null)
        {
            throw new ServiceException("查询平台订单返回空结果");
        }
        return data;
    }

    /** 查询平台订单详情及订单处理记录。 */
    public com.ruoyi.system.api.model.PlatformOrderDetailView getPlatformOrderDetail(Long orderId)
    {
        R<com.ruoyi.system.api.model.PlatformOrderDetailView> response = remoteRentalBusinessService
                .getPlatformOrderDetail(orderId, SecurityConstants.INNER);
        com.ruoyi.system.api.model.PlatformOrderDetailView data = require(response, "查询平台订单详情");
        if (data == null)
        {
            throw new ServiceException("查询平台订单详情返回空结果");
        }
        return data;
    }

    /** 查询指定商户的经营统计。 */
    public List<PlatformMerchantOverviewStat> listPlatformMerchantOverviewStats(List<Long> memberIds)
    {
        if (memberIds == null || memberIds.isEmpty())
        {
            return Collections.emptyList();
        }
        R<List<PlatformMerchantOverviewStat>> response = remoteRentalBusinessService
                .listPlatformMerchantOverviewStats(memberIds, SecurityConstants.INNER);
        List<PlatformMerchantOverviewStat> data = require(response, "查询商户经营概况");
        return data == null ? Collections.emptyList() : data;
    }

    /** 查询指定商户的经营明细。 */
    public PlatformMerchantBusinessView getPlatformMerchantBusiness(Long tenantId, Long memberId)
    {
        R<PlatformMerchantBusinessView> response = remoteRentalBusinessService.getPlatformMerchantBusiness(tenantId,
                memberId, SecurityConstants.INNER);
        PlatformMerchantBusinessView data = require(response, "查询商户经营明细");
        if (data == null)
        {
            throw new ServiceException("查询商户经营明细返回空结果");
        }
        return data;
    }

    /** 查询指定商户的履约、物流和风险治理汇总。 */
    public PlatformMerchantGovernanceView getPlatformMerchantGovernance(Long tenantId, Long memberId)
    {
        R<PlatformMerchantGovernanceView> response = remoteRentalBusinessService.getPlatformMerchantGovernance(
                tenantId, memberId, SecurityConstants.INNER);
        PlatformMerchantGovernanceView data = require(response, "查询商户履约物流风险治理");
        return data == null ? new PlatformMerchantGovernanceView() : data;
    }

    /** 查询指定商户范围的经营分析聚合结果。 */
    public PlatformOperationsAnalyticsView getPlatformOperationsAnalytics(List<Long> memberIds, String beginDate,
            String endDate)
    {
        R<PlatformOperationsAnalyticsView> response = remoteRentalBusinessService.getPlatformOperationsAnalytics(
                memberIds, beginDate, endDate, SecurityConstants.INNER);
        PlatformOperationsAnalyticsView data = require(response, "查询平台经营分析");
        if (data == null)
        {
            throw new ServiceException("查询平台经营分析返回空结果");
        }
        return data;
    }

    /** 查询平台执行面待办汇总。 */
    public PlatformExecutionWorkbenchView getPlatformExecutionWorkbench()
    {
        R<PlatformExecutionWorkbenchView> response = remoteRentalBusinessService
                .getPlatformExecutionWorkbench(SecurityConstants.INNER);
        PlatformExecutionWorkbenchView data = require(response, "查询平台执行面待办");
        if (data == null)
        {
            throw new ServiceException("查询平台执行面待办返回空结果");
        }
        return data;
    }

    /** 查询平台联盟订单治理分页数据。 */
    public PlatformAllianceOrderPageView listPlatformAllianceOrders(
            com.ruoyi.system.api.model.PlatformAllianceOrderQuery query)
    {
        R<PlatformAllianceOrderPageView> response = remoteRentalBusinessService.listPlatformAllianceOrders(
                query.getAllianceId(), query.getSourceMemberId(), query.getTargetMemberId(), query.getStatus(), query.getViewMode(),
                query.getBeginTime(), query.getEndTime(), query.getPageNum(), query.getPageSize(),
                SecurityConstants.INNER);
        PlatformAllianceOrderPageView data = require(response, "查询平台联盟订单");
        if (data == null)
        {
            throw new ServiceException("查询平台联盟订单返回空结果");
        }
        return data;
    }

    /** 查询平台联盟订单详情、事件和结算记录。 */
    public PlatformAllianceOrderDetailView getPlatformAllianceOrderDetail(Long allianceOrderId)
    {
        R<PlatformAllianceOrderDetailView> response = remoteRentalBusinessService
                .getPlatformAllianceOrderDetail(allianceOrderId, SecurityConstants.INNER);
        PlatformAllianceOrderDetailView data = require(response, "查询联盟订单详情");
        if (data == null)
        {
            throw new ServiceException("查询联盟订单详情返回空结果");
        }
        return data;
    }

    /** 查询平台联盟结算汇总。 */
    public PlatformAllianceSettlementSummaryView getPlatformAllianceSettlementSummary()
    {
        R<PlatformAllianceSettlementSummaryView> response = remoteRentalBusinessService
                .getPlatformAllianceSettlementSummary(SecurityConstants.INNER);
        PlatformAllianceSettlementSummaryView data = require(response, "查询联盟结算汇总");
        return data == null ? new PlatformAllianceSettlementSummaryView() : data;
    }

    /** 查询平台联盟纠纷治理分页数据。 */
    public PlatformAllianceDisputePageView listPlatformAllianceDisputes(Integer status, Integer pageNum,
            Integer pageSize)
    {
        R<PlatformAllianceDisputePageView> response = remoteRentalBusinessService
                .listPlatformAllianceDisputes(status, pageNum, pageSize, SecurityConstants.INNER);
        PlatformAllianceDisputePageView data = require(response, "查询平台联盟纠纷");
        if (data == null) throw new ServiceException("查询平台联盟纠纷返回空结果");
        return data;
    }

    /** 查询平台联盟纠纷详情。 */
    public PlatformAllianceDisputeView getPlatformAllianceDisputeDetail(Long disputeId)
    {
        R<PlatformAllianceDisputeView> response = remoteRentalBusinessService
                .getPlatformAllianceDisputeDetail(disputeId, SecurityConstants.INNER);
        PlatformAllianceDisputeView data = require(response, "查询联盟纠纷详情");
        if (data == null) throw new ServiceException("查询联盟纠纷详情返回空结果");
        return data;
    }

    /** 提交平台联盟纠纷调解方案。 */
    public void proposePlatformAllianceMediation(PlatformAllianceMediationCommand command, Long mediatorUserId)
    {
        R<Void> response = remoteRentalBusinessService.proposePlatformAllianceMediation(command, mediatorUserId,
                SecurityConstants.INNER);
        require(response, "提交联盟调解方案");
    }

    /** 查询平台售后案件分页数据。 */
    public PlatformAfterSalesPageView listPlatformAfterSales(Integer caseStatus, String keyword,
            String merchantKeyword, Integer pageNum, Integer pageSize)
    {
        R<PlatformAfterSalesPageView> response = remoteRentalBusinessService.listPlatformAfterSales(caseStatus,
                keyword, merchantKeyword, pageNum, pageSize, SecurityConstants.INNER);
        PlatformAfterSalesPageView data = require(response, "查询平台售后案件");
        if (data == null)
        {
            throw new ServiceException("查询平台售后案件返回空结果");
        }
        return data;
    }

    /** 查询平台售后案件详情。 */
    public PlatformAfterSalesDetailView getPlatformAfterSalesDetail(Long caseId)
    {
        R<PlatformAfterSalesDetailView> response = remoteRentalBusinessService
                .getPlatformAfterSalesDetail(caseId, SecurityConstants.INNER);
        PlatformAfterSalesDetailView data = require(response, "查询平台售后详情");
        if (data == null)
        {
            throw new ServiceException("查询平台售后详情返回空结果");
        }
        return data;
    }

    /** 追加平台售后处理事件。 */
    public void appendPlatformAfterSalesEvent(Long caseId, PlatformAfterSalesEventCommand command, Long operatorId,
            String operatorName)
    {
        R<Void> response = remoteRentalBusinessService.appendPlatformAfterSalesEvent(caseId, command, operatorId,
                operatorName, SecurityConstants.INNER);
        require(response, "处理平台售后案件");
    }

    /** 追加平台售后证据。 */
    public void appendPlatformAfterSalesEvidence(Long caseId, PlatformAfterSalesEvidenceCommand command,
            Long operatorId, String operatorName)
    {
        R<Void> response = remoteRentalBusinessService.appendPlatformAfterSalesEvidence(caseId, command, operatorId,
                operatorName, SecurityConstants.INNER);
        require(response, "保存平台售后证据");
    }

    /** 查询平台物流台账。 */
    public PlatformLogisticsPageView listPlatformLogistics(String keyword, String merchantKeyword, Integer direction,
            Integer routeStatus, Boolean abnormalOnly, Integer pageNum, Integer pageSize)
    {
        R<PlatformLogisticsPageView> response = remoteRentalBusinessService.listPlatformLogistics(keyword,
                merchantKeyword, direction, routeStatus, abnormalOnly, pageNum, pageSize, SecurityConstants.INNER);
        PlatformLogisticsPageView data = require(response, "查询平台物流台账");
        if (data == null)
        {
            throw new ServiceException("查询平台物流台账返回空结果");
        }
        return data;
    }

    /** 查询平台物流详情和已缓存路线。 */
    public PlatformLogisticsDetailView getPlatformLogisticsDetail(Long logisticsId)
    {
        R<PlatformLogisticsDetailView> response = remoteRentalBusinessService
                .getPlatformLogisticsDetail(logisticsId, SecurityConstants.INNER);
        PlatformLogisticsDetailView data = require(response, "查询平台物流详情");
        if (data == null)
        {
            throw new ServiceException("查询平台物流详情返回空结果");
        }
        return data;
    }

    /** 手工刷新平台选中的单条物流路线。 */
    public int refreshPlatformLogistics(Long logisticsId)
    {
        R<Integer> response = remoteRentalBusinessService.refreshPlatformLogistics(logisticsId,
                SecurityConstants.INNER);
        Integer refreshed = require(response, "刷新单条物流路线");
        return refreshed == null ? 0 : refreshed.intValue();
    }

    /** 触发未签收物流路线批量刷新。 */
    public int refreshLogisticsRoutes()
    {
        R<Integer> response = remoteRentalBusinessService.refreshLogisticsRoutes(SecurityConstants.INNER);
        Integer refreshed = require(response, "刷新物流路线");
        return refreshed == null ? 0 : refreshed.intValue();
    }

    /** 查询平台联盟订单渠道。 */
    public List<PlatformOrderChannelVO> listPlatformOrderChannels(String keyword, Integer status)
    {
        R<List<PlatformOrderChannelView>> response = remoteRentalBusinessService.listPlatformOrderChannels(keyword,
                status, SecurityConstants.INNER);
        List<PlatformOrderChannelView> data = require(response, "查询平台订单渠道");
        return toPlatformOrderChannelViews(data);
    }

    /** 新增平台联盟订单渠道。 */
    public PlatformOrderChannelVO addPlatformOrderChannel(PlatformOrderChannelCommand command)
    {
        R<PlatformOrderChannelView> response = remoteRentalBusinessService.addPlatformOrderChannel(command,
                SecurityConstants.INNER);
        return toPlatformOrderChannelView(require(response, "新增平台订单渠道"));
    }

    /** 修改平台联盟订单渠道。 */
    public PlatformOrderChannelVO updatePlatformOrderChannel(PlatformOrderChannelCommand command)
    {
        R<PlatformOrderChannelView> response = remoteRentalBusinessService.updatePlatformOrderChannel(command,
                SecurityConstants.INNER);
        return toPlatformOrderChannelView(require(response, "修改平台订单渠道"));
    }

    /** 软删除平台联盟订单渠道。 */
    public boolean deletePlatformOrderChannel(Long channelId)
    {
        R<Boolean> response = remoteRentalBusinessService.deletePlatformOrderChannel(channelId,
                SecurityConstants.INNER);
        Boolean data = require(response, "删除平台订单渠道");
        if (data == null)
        {
            throw new ServiceException("删除平台订单渠道返回空结果");
        }
        return data.booleanValue();
    }

    /** 查询平台协议版本。 */
    public List<PlatformProtocolVO> listPlatformProtocols(String protocolKey)
    {
        R<List<PlatformProtocolView>> response = remoteRentalBusinessService.listPlatformProtocols(protocolKey,
                SecurityConstants.INNER);
        List<PlatformProtocolView> data = require(response, "查询平台协议版本");
        return toPlatformProtocolViews(data);
    }

    /** 新增平台协议草稿。 */
    public PlatformProtocolVO addPlatformProtocol(PlatformProtocolCommand command)
    {
        R<PlatformProtocolView> response = remoteRentalBusinessService.addPlatformProtocol(command,
                SecurityConstants.INNER);
        return toPlatformProtocolView(require(response, "新增平台协议版本"));
    }

    /** 修改平台协议草稿。 */
    public PlatformProtocolVO updatePlatformProtocol(PlatformProtocolCommand command)
    {
        R<PlatformProtocolView> response = remoteRentalBusinessService.updatePlatformProtocol(command,
                SecurityConstants.INNER);
        return toPlatformProtocolView(require(response, "修改平台协议版本"));
    }

    /** 发布平台协议版本。 */
    public PlatformProtocolVO publishPlatformProtocol(Long protocolId, String effectiveTime)
    {
        R<PlatformProtocolView> response = remoteRentalBusinessService.publishPlatformProtocol(protocolId,
                effectiveTime, SecurityConstants.INNER);
        return toPlatformProtocolView(require(response, "发布平台协议版本"));
    }

    private List<PlatformOrderChannelVO> toPlatformOrderChannelViews(List<PlatformOrderChannelView> sources)
    {
        if (sources == null || sources.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformOrderChannelVO> result = new java.util.ArrayList<PlatformOrderChannelVO>(sources.size());
        for (PlatformOrderChannelView source : sources)
        {
            result.add(toPlatformOrderChannelView(source));
        }
        return result;
    }

    private PlatformOrderChannelVO toPlatformOrderChannelView(PlatformOrderChannelView source)
    {
        if (source == null)
        {
            return null;
        }
        PlatformOrderChannelVO target = new PlatformOrderChannelVO();
        target.setChannelId(source.getChannelId());
        target.setChannelCode(source.getChannelCode());
        target.setChannelName(source.getChannelName());
        target.setChannelType(source.getChannelType());
        target.setScopeType(source.getScopeType());
        target.setStatus(source.getStatus());
        target.setSortNo(source.getSortNo());
        return target;
    }

    private List<PlatformProtocolVO> toPlatformProtocolViews(List<PlatformProtocolView> sources)
    {
        if (sources == null || sources.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformProtocolVO> result = new java.util.ArrayList<PlatformProtocolVO>(sources.size());
        for (PlatformProtocolView source : sources)
        {
            result.add(toPlatformProtocolView(source));
        }
        return result;
    }

    private PlatformProtocolVO toPlatformProtocolView(PlatformProtocolView source)
    {
        if (source == null)
        {
            return null;
        }
        PlatformProtocolVO target = new PlatformProtocolVO();
        target.setProtocolId(source.getProtocolId());
        target.setProtocolKey(source.getProtocolKey());
        target.setVersionNo(source.getVersionNo());
        target.setProtocolTitle(source.getProtocolTitle());
        target.setProtocolContent(source.getProtocolContent());
        target.setContentHash(source.getContentHash());
        target.setProtocolStatus(source.getProtocolStatus());
        target.setEffectiveTime(source.getEffectiveTime());
        target.setScopeTypes(source.getScopeTypes());
        target.setScopeLabels(source.getScopeLabels());
        return target;
    }

    /**
     * 统一解包内部服务响应，任何非成功状态都必须阻断当前平台操作。
     *
     * @param response 远程服务响应
     * @param operation 当前操作名称
     * @param <T> 响应数据类型
     * @return 远程响应数据
     */
    private <T> T require(R<T> response, String operation)
    {
        if (response == null || !R.isSuccess(response))
        {
            String message = response == null ? "无响应" : response.getMsg();
            throw new ServiceException(operation + "失败：" + message);
        }
        return response.getData();
    }
}
