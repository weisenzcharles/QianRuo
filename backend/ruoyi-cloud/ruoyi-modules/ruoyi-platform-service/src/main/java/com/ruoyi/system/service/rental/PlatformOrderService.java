package com.ruoyi.system.service.rental;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.system.api.model.PlatformOrderPageView;
import com.ruoyi.system.api.model.PlatformOrderView;
import com.ruoyi.system.api.model.PlatformOrderDetailView;
import com.ruoyi.system.api.model.PlatformOrderNoteView;
import com.ruoyi.system.api.model.PlatformOrderEditLogView;
import com.ruoyi.system.api.model.PlatformAllianceOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderView;
import com.ruoyi.system.api.model.PlatformAllianceOrderDetailView;
import com.ruoyi.system.api.model.PlatformAllianceDisputePageView;
import com.ruoyi.system.api.model.PlatformAllianceDisputeView;
import com.ruoyi.system.api.model.PlatformAllianceMediationCommand;
import com.ruoyi.system.api.model.PlatformAllianceSettlementSummaryView;
import com.ruoyi.system.domain.rental.RentalCompensationRule;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.system.api.model.PlatformAllianceScopeView;
import com.ruoyi.system.api.model.PlatformAfterSalesDetailView;
import com.ruoyi.system.api.model.PlatformAfterSalesEventView;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceView;
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.api.model.PlatformLogisticsRouteView;
import com.ruoyi.system.api.model.PlatformOrderItemView;
import com.ruoyi.system.api.model.PlatformOrderReturnInspectionView;
import com.ruoyi.system.model.rental.PlatformOrderVO;
import com.ruoyi.system.model.rental.PlatformOrderDetailVO;
import com.ruoyi.system.model.rental.PlatformOrderItemVO;
import com.ruoyi.system.model.rental.PlatformOrderReturnInspectionVO;
import com.ruoyi.system.model.rental.PlatformOrderLogisticsVO;
import com.ruoyi.system.model.rental.PlatformOrderRouteVO;
import com.ruoyi.system.model.rental.PlatformOrderAfterSalesVO;
import com.ruoyi.system.model.rental.PlatformOrderAfterSalesEventVO;
import com.ruoyi.system.model.rental.PlatformOrderAfterSalesEvidenceVO;
import com.ruoyi.system.model.rental.PlatformOrderNoteVO;
import com.ruoyi.system.model.rental.PlatformOrderEditLogVO;
import com.ruoyi.system.model.rental.PlatformOrderCustomerVO;

/** 平台订单查询编排服务，负责商铺筛选和名称补全。 */
@Service
public class PlatformOrderService
{
    private final RentalMemberMapper memberMapper;
    private final RentalBusinessGateway rentalBusinessGateway;
    private final RentalCompensationRuleService compensationRuleService;

    public PlatformOrderService(RentalMemberMapper memberMapper, RentalBusinessGateway rentalBusinessGateway,
            RentalCompensationRuleService compensationRuleService)
    {
        this.memberMapper = memberMapper;
        this.rentalBusinessGateway = rentalBusinessGateway;
        this.compensationRuleService = compensationRuleService;
    }

    /** 按平台筛选条件分页查询全部商户订单，并转换为平台浏览器展示对象。 */
    public TableDataInfo list(String shopName, String keyword, Integer orderStatus, String beginTime, String endTime,
            Integer pageNum, Integer pageSize)
    {
        List<Long> memberIds = null;
        if (shopName != null && !shopName.trim().isEmpty())
        {
            List<Long> matchedMemberIds = memberMapper.selectRentalMemberIdsByKeyword(shopName.trim());
            List<Long> matchedMembers = matchedMemberIds == null ? Collections.emptyList() : matchedMemberIds;
            memberIds = new ArrayList<Long>(matchedMembers.size());
            memberIds.addAll(matchedMembers);
            if (memberIds.isEmpty())
            {
                return new TableDataInfo(Collections.emptyList(), 0L);
            }
        }

        PlatformOrderPageView page = rentalBusinessGateway.listPlatformOrders(memberIds, keyword, orderStatus, beginTime,
                endTime, pageNum, pageSize);
        enrichMerchantNames(page.getRows());
        return new TableDataInfo(toBrowserRows(page.getRows()), page.getTotal());
    }

    /** 查询平台订单详情并对客户联系方式和地址执行默认脱敏。 */
    public PlatformOrderDetailVO detail(Long orderId)
    {
        PlatformOrderDetailView source = rentalBusinessGateway.getPlatformOrderDetail(orderId);
        if (source == null || source.getOrder() == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("平台订单详情不存在");
        }
        PlatformOrderView order = source.getOrder();
        if (order.getMemberId() != null)
        {
            List<RentalMember> members = memberMapper.selectRentalMembersByIds(
                    Collections.singletonList(order.getMemberId()));
            if (members != null && !members.isEmpty())
            {
                RentalMember member = members.get(0);
                order.setMerchantName(text(member.getNickname(),
                        text(member.getLegalSubjectName(), member.getMemberCode())));
            }
        }
        PlatformOrderDetailVO result = toBrowserDetail(order);
        result.setItems(toBrowserItems(source.getItems()));
        result.setReturnInspection(toBrowserReturnInspection(source.getReturnInspection()));
        result.setLogistics(toBrowserLogistics(source.getLogistics()));
        result.setAfterSales(toBrowserAfterSales(source.getAfterSales()));
        result.setNotes(toBrowserNotes(source.getNotes()));
        result.setEditLogs(toBrowserEditLogs(source.getEditLogs()));
        return result;
    }

    /** 查询平台订单完整客户信息，调用方权限和审计由控制层统一处理。 */
    public PlatformOrderCustomerVO customerDetail(Long orderId)
    {
        if (orderId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单唯一编号不能为空");
        }
        PlatformOrderDetailView source = rentalBusinessGateway.getPlatformOrderDetail(orderId);
        if (source == null || source.getOrder() == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("平台订单详情不存在");
        }
        return toBrowserCustomer(source.getOrder());
    }

    /** 将完整客户字段映射为独立最小视图，避免敏感接口透传内部订单对象。 */
    static PlatformOrderCustomerVO toBrowserCustomer(PlatformOrderView row)
    {
        if (row == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("平台订单详情不存在");
        }
        PlatformOrderCustomerVO result = new PlatformOrderCustomerVO();
        result.setOrderNo(text(row.getOrderNo(), "-"));
        result.setCustomerName(text(row.getCustomerName(), "-"));
        result.setCustomerPhone(text(row.getCustomerPhone(), "-"));
        result.setRecipientName(text(row.getRecipientName(), "-"));
        result.setRecipientPhone(text(row.getRecipientPhone(), "-"));
        result.setRecipientAddress(text(row.getRecipientAddress(), "-"));
        return result;
    }

    /** 将归还验机事实转换为平台页面可读字段，不向浏览器暴露内部编号或原始枚举值。 */
    private PlatformOrderReturnInspectionVO toBrowserReturnInspection(PlatformOrderReturnInspectionView source)
    {
        if (source == null)
        {
            return null;
        }
        PlatformOrderReturnInspectionVO result = new PlatformOrderReturnInspectionVO();
        result.setResultLabel(text(source.getResultLabel(), "未知结果"));
        result.setCaseTypeLabel(text(source.getCaseTypeLabel(), "-") );
        result.setProblemDesc(text(source.getProblemDesc(), "-") );
        result.setLostItemsJson(text(source.getLostItemsJson(), "-") );
        result.setCompensationAmount(source.getCompensationAmount());
        result.setCustomerContactedLabel(contactedLabel(source.getCustomerContacted()));
        result.setRemark(text(source.getRemark(), "-") );
        result.setInspectedTime(text(source.getInspectedTime(), "-") );
        return result;
    }

    private String contactedLabel(Integer value)
    {
        if (value == null)
        {
            return "未记录";
        }
        return value.intValue() == 1 ? "已联系" : "未联系";
    }

    /** 查询平台联盟订单并补全双方商户名称。 */
    public PlatformAllianceOrderPageView listAlliance(com.ruoyi.system.api.model.PlatformAllianceOrderQuery query)
    {
        PlatformAllianceOrderPageView page = rentalBusinessGateway.listPlatformAllianceOrders(query);
        if (page.getRows() == null || page.getRows().isEmpty())
        {
            return page;
        }
        Set<Long> memberIds = new LinkedHashSet<Long>();
        for (PlatformAllianceOrderView row : page.getRows())
        {
            if (row.getSourceMemberId() != null) memberIds.add(row.getSourceMemberId());
            if (row.getTargetMemberId() != null) memberIds.add(row.getTargetMemberId());
        }
        Map<Long, String> names = new HashMap<Long, String>();
        for (RentalMember member : memberMapper.selectRentalMembersByIds(new ArrayList<Long>(memberIds)))
        {
            names.put(member.getMemberId(), text(member.getNickname(), text(member.getLegalSubjectName(), member.getMemberCode())));
        }
        for (PlatformAllianceOrderView row : page.getRows())
        {
            row.setSourceMerchantName(text(names.get(row.getSourceMemberId()), "未知商户"));
            row.setTargetMerchantName(text(names.get(row.getTargetMemberId()), "未知商户"));
        }
        return page;
    }

    /** 查询平台联盟订单详情并补全双方商户名称。 */
    public PlatformAllianceOrderDetailView detailAlliance(Long allianceOrderId)
    {
        PlatformAllianceOrderDetailView detail = rentalBusinessGateway
                .getPlatformAllianceOrderDetail(allianceOrderId);
        PlatformAllianceOrderView order = detail.getOrder();
        List<Long> memberIds = new ArrayList<Long>();
        if (order.getSourceMemberId() != null) memberIds.add(order.getSourceMemberId());
        if (order.getTargetMemberId() != null && !memberIds.contains(order.getTargetMemberId()))
        {
            memberIds.add(order.getTargetMemberId());
        }
        if (detail.getEvents() != null)
        {
            for (com.ruoyi.system.api.model.PlatformAllianceOrderEventView event : detail.getEvents())
            {
                if (event.getOperatorMemberId() != null && !memberIds.contains(event.getOperatorMemberId()))
                {
                    memberIds.add(event.getOperatorMemberId());
                }
            }
        }
        Map<Long, String> names = new HashMap<Long, String>();
        if (!memberIds.isEmpty())
        {
            for (RentalMember member : memberMapper.selectRentalMembersByIds(memberIds))
            {
                names.put(member.getMemberId(), text(member.getNickname(),
                        text(member.getLegalSubjectName(), member.getMemberCode())));
            }
        }
        order.setSourceMerchantName(text(names.get(order.getSourceMemberId()), "未知商户"));
        order.setTargetMerchantName(text(names.get(order.getTargetMemberId()), "未知商户"));
        if (detail.getEvents() != null)
        {
            for (com.ruoyi.system.api.model.PlatformAllianceOrderEventView event : detail.getEvents())
            {
                event.setOperatorMerchantName(event.getOperatorMemberId() == null
                        ? "系统" : text(names.get(event.getOperatorMemberId()), "未知商户"));
            }
        }
        return detail;
    }

    /** 查询平台联盟结算汇总。 */
    public PlatformAllianceSettlementSummaryView allianceSettlementSummary()
    {
        return rentalBusinessGateway.getPlatformAllianceSettlementSummary();
    }

    /** 查询平台联盟纠纷治理分页数据。 */
    public PlatformAllianceDisputePageView listAllianceDisputes(Integer status, Integer pageNum, Integer pageSize)
    {
        PlatformAllianceDisputePageView page = rentalBusinessGateway.listPlatformAllianceDisputes(status, pageNum, pageSize);
        enrichAllianceDisputes(page.getRows());
        return page;
    }

    /** 查询平台联盟纠纷详情。 */
    public PlatformAllianceDisputeView detailAllianceDispute(Long disputeId)
    {
        PlatformAllianceDisputeView detail = rentalBusinessGateway.getPlatformAllianceDisputeDetail(disputeId);
        if (detail != null) enrichAllianceDisputes(Collections.singletonList(detail));
        return detail;
    }

    /** 将联盟纠纷中的商户关联编号转换为平台可读的商户名称。 */
    private void enrichAllianceDisputes(List<PlatformAllianceDisputeView> rows)
    {
        if (rows == null || rows.isEmpty()) return;
        Set<Long> memberIds = new LinkedHashSet<Long>();
        for (PlatformAllianceDisputeView row : rows)
        {
            if (row.getInitiatorMemberId() != null) memberIds.add(row.getInitiatorMemberId());
            if (row.getRespondentMemberId() != null) memberIds.add(row.getRespondentMemberId());
        }
        Map<Long, String> names = new HashMap<Long, String>();
        for (RentalMember member : memberMapper.selectRentalMembersByIds(new ArrayList<Long>(memberIds)))
        {
            names.put(member.getMemberId(), text(member.getNickname(), text(member.getLegalSubjectName(), member.getMemberCode())));
        }
        for (PlatformAllianceDisputeView row : rows)
        {
            row.setInitiatorMerchantName(text(names.get(row.getInitiatorMemberId()), "未知商户"));
            row.setRespondentMerchantName(text(names.get(row.getRespondentMemberId()), "未知商户"));
        }
    }

    /** 平台提交联盟纠纷调解方案。 */
    public void proposeAllianceMediation(PlatformAllianceMediationCommand command)
    {
        if (command == null || command.getDisputeId() == null || command.getProposalAmount() == null
                || command.getProposalAmount().signum() < 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("调解赔付金额不合法");
        }
        PlatformAllianceDisputeView dispute = detailAllianceDispute(command.getDisputeId());
        RentalCompensationRule rule = compensationRuleService.applicable(dispute.getDisputeType());
        if (rule != null && command.getProposalAmount().compareTo(rule.getMaxAmount()) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("调解赔付金额超过当前赔付规则上限");
        }
        rentalBusinessGateway.proposePlatformAllianceMediation(command, SecurityUtils.getUserId());
    }

    /** 查询平台联盟管理分页数据。 */
    public TableDataInfo listMerchantAlliances(String keyword, Integer status, Integer pageNum, Integer pageSize)
    {
        return rentalBusinessGateway.listMerchantAlliances(keyword, status, pageNum, pageSize);
    }

    /** 查询平台联盟成员。 */
    public List<MerchantAllianceOption> listMerchantAllianceMembers(Long allianceId)
    {
        return rentalBusinessGateway.listMerchantAllianceMembers(allianceId);
    }

    /** 查询联盟成员共享范围。 */
    public List<PlatformAllianceScopeView> listMerchantAllianceScopes(Long allianceId, Long memberId)
    {
        RentalMember member = memberMapper.selectRentalMemberById(memberId);
        if (member == null || member.getTenantId() == null)
        {
            return Collections.emptyList();
        }
        return rentalBusinessGateway.listMerchantAllianceScopes(member.getTenantId(), allianceId, memberId);
    }

    /** 更新平台联盟或成员状态。 */
    public void updateMerchantAllianceStatus(Long allianceId, Long memberId, Integer status)
    {
        rentalBusinessGateway.updateMerchantAllianceStatus(allianceId, memberId, status);
    }

    /** 仅查询当前页商户名称，避免对全部商户产生额外加载。 */
    private void enrichMerchantNames(List<PlatformOrderView> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return;
        }
        Set<Long> memberIds = new LinkedHashSet<Long>();
        for (PlatformOrderView row : rows)
        {
            if (row.getMemberId() != null)
            {
                memberIds.add(row.getMemberId());
            }
        }
        if (memberIds.isEmpty())
        {
            return;
        }
        List<RentalMember> members = memberMapper.selectRentalMembersByIds(new ArrayList<Long>(memberIds));
        Map<Long, String> names = new HashMap<Long, String>();
        for (RentalMember member : members)
        {
            String name = text(member.getNickname(), text(member.getLegalSubjectName(), member.getMemberCode()));
            names.put(member.getMemberId(), name);
        }
        for (PlatformOrderView row : rows)
        {
            row.setMerchantName(text(names.get(row.getMemberId()), "未知商户"));
        }
    }

    /** 将内部订单视图转换为最小化的浏览器响应，并对客户电话执行默认脱敏。 */
    private List<PlatformOrderVO> toBrowserRows(List<PlatformOrderView> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformOrderVO> result = new ArrayList<PlatformOrderVO>(rows.size());
        for (PlatformOrderView row : rows)
        {
            PlatformOrderVO view = new PlatformOrderVO();
            view.setOrderId(row.getOrderId());
            view.setOrderNo(row.getOrderNo());
            view.setMerchantName(text(row.getMerchantName(), "未知商户"));
            view.setSourceShopName(row.getSourceShopName());
            view.setCustomerName(row.getCustomerName());
            view.setCustomerPhone(maskPhone(row.getCustomerPhone()));
            view.setRecipientName(row.getRecipientName());
            view.setRecipientPhone(maskPhone(row.getRecipientPhone()));
            view.setLeaseStartDate(row.getLeaseStartDate());
            view.setLeaseEndDate(row.getLeaseEndDate());
            view.setRentAmount(row.getRentAmount());
            view.setDepositAmount(row.getDepositAmount());
            view.setOrderStatus(row.getOrderStatus());
            view.setCreateTime(row.getCreateTime());
            result.add(view);
        }
        return result;
    }

    private PlatformOrderDetailVO toBrowserDetail(PlatformOrderView row)
    {
        PlatformOrderDetailVO result = new PlatformOrderDetailVO();
        result.setOrderNo(row.getOrderNo());
        result.setMerchantName(text(row.getMerchantName(), "未知商户"));
        result.setSourceShopName(row.getSourceShopName());
        result.setExternalOrderNo(row.getExternalOrderNo());
        result.setCustomerName(row.getCustomerName());
        result.setCustomerPhone(maskPhone(row.getCustomerPhone()));
        result.setRecipientName(row.getRecipientName());
        result.setRecipientPhone(maskPhone(row.getRecipientPhone()));
        result.setRecipientAddress(maskAddress(row.getRecipientAddress()));
        result.setLeaseStartDate(row.getLeaseStartDate());
        result.setLeaseEndDate(row.getLeaseEndDate());
        result.setShipDueTime(row.getShipDueTime());
        result.setReturnDueTime(row.getReturnDueTime());
        result.setArrivalDate(row.getArrivalDate());
        result.setLeaseDays(row.getLeaseDays());
        result.setWarehouseName(row.getWarehouseName());
        result.setReturnWarehouseName(row.getReturnWarehouseName());
        result.setWarehouseCity(row.getWarehouseCity());
        result.setProductName(row.getProductName());
        result.setModelName(row.getModelName());
        result.setProductCode(row.getProductCode());
        result.setPackageName(row.getPackageName());
        result.setPackageDetailsJson(row.getPackageDetailsJson());
        result.setShippingCarrier(row.getShippingCarrier());
        result.setShippingTrackingNo(row.getShippingTrackingNo());
        result.setReturnCarrier(row.getReturnCarrier());
        result.setReturnTrackingNo(row.getReturnTrackingNo());
        result.setRentAmount(row.getRentAmount());
        result.setDepositAmount(row.getDepositAmount());
        result.setDeliveryAmount(row.getDeliveryAmount());
        result.setInsuranceAmount(row.getInsuranceAmount());
        result.setOrderStatus(row.getOrderStatus());
        result.setPayStatus(row.getPayStatus());
        result.setTimeEffectStatus(row.getTimeEffectStatus());
        result.setPriorityFlag(row.getPriorityFlag());
        result.setWeatherRiskFlag(row.getWeatherRiskFlag());
        result.setCustomerNote(row.getCustomerNote());
        result.setRemark(row.getRemark());
        result.setCreateTime(row.getCreateTime());
        return result;
    }

    private List<PlatformOrderNoteVO> toBrowserNotes(List<PlatformOrderNoteView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderNoteVO> result = new ArrayList<PlatformOrderNoteVO>(rows.size());
        for (PlatformOrderNoteView row : rows)
        {
            PlatformOrderNoteVO item = new PlatformOrderNoteVO();
            item.setNoteContent(row.getNoteContent());
            item.setNoteType(row.getNoteType());
            item.setOperatorName(row.getOperatorName());
            item.setCreateTime(row.getCreateTime());
            result.add(item);
        }
        return result;
    }

    private List<PlatformOrderItemVO> toBrowserItems(List<PlatformOrderItemView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderItemVO> result = new ArrayList<PlatformOrderItemVO>(rows.size());
        for (PlatformOrderItemView row : rows)
        {
            if (row == null) continue;
            PlatformOrderItemVO item = new PlatformOrderItemVO();
            item.setItemTypeName(text(row.getItemTypeName(), "套餐明细"));
            item.setItemName(text(row.getItemName(), "未命名商品"));
            item.setItemCode(text(row.getItemCode(), "-"));
            item.setModelName(text(row.getModelName(), "-"));
            item.setQuantity(row.getQuantity() == null ? Integer.valueOf(1) : row.getQuantity());
            item.setItemStatusName(text(row.getItemStatusName(), "待处理"));
            item.setSerialNumbers(row.getSerialNumbers());
            item.setInventoryCodes(row.getInventoryCodes());
            result.add(item);
        }
        return result;
    }

    private List<PlatformOrderLogisticsVO> toBrowserLogistics(List<PlatformLogisticsDetailView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderLogisticsVO> result = new ArrayList<PlatformOrderLogisticsVO>(rows.size());
        for (PlatformLogisticsDetailView detail : rows)
        {
            if (detail == null || detail.getLogistics() == null) continue;
            com.ruoyi.system.api.model.PlatformLogisticsView source = detail.getLogistics();
            PlatformOrderLogisticsVO item = new PlatformOrderLogisticsVO();
            item.setDirectionName(text(source.getDirectionName(), "未知方向"));
            item.setCarrier(text(source.getCarrier(), "-") );
            item.setTrackingNo(text(source.getTrackingNo(), "-"));
            item.setSourceTypeName(text(source.getSourceTypeName(), "未知来源"));
            item.setRouteStatusName(text(source.getRouteStatusName(), "未知状态"));
            item.setRouteProvider(text(source.getRouteProvider(), "-"));
            item.setRouteMessage(text(source.getRouteMessage(), "-"));
            item.setRouteQueryTime(text(source.getRouteQueryTime(), "-"));
            item.setLastRouteTime(text(source.getLastRouteTime(), "-"));
            item.setFeeAmount(source.getFeeAmount());
            item.setRoutes(toBrowserRoutes(detail.getRoutes()));
            result.add(item);
        }
        return result;
    }

    private List<PlatformOrderRouteVO> toBrowserRoutes(List<PlatformLogisticsRouteView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderRouteVO> result = new ArrayList<PlatformOrderRouteVO>(rows.size());
        for (PlatformLogisticsRouteView row : rows)
        {
            if (row == null) continue;
            PlatformOrderRouteVO item = new PlatformOrderRouteVO();
            item.setRouteStatusName(text(row.getRouteStatusName(), "未知状态"));
            item.setRouteDesc(text(row.getRouteDesc(), "-"));
            item.setRouteTime(text(row.getRouteTime(), "-"));
            item.setLocation(text(row.getLocation(), "-"));
            result.add(item);
        }
        return result;
    }

    private List<PlatformOrderAfterSalesVO> toBrowserAfterSales(List<PlatformAfterSalesDetailView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderAfterSalesVO> result = new ArrayList<PlatformOrderAfterSalesVO>(rows.size());
        for (PlatformAfterSalesDetailView detail : rows)
        {
            if (detail == null || detail.getAfterSales() == null) continue;
            com.ruoyi.system.api.model.PlatformAfterSalesView source = detail.getAfterSales();
            PlatformOrderAfterSalesVO item = new PlatformOrderAfterSalesVO();
            item.setCaseNo(text(source.getCaseNo(), "-") );
            item.setCaseTypeName(text(source.getCaseTypeName(), "未知类型"));
            item.setCaseStatusName(text(source.getCaseStatusName(), "未知状态"));
            item.setProblemDesc(text(source.getProblemDesc(), "-"));
            item.setCompensationAmount(source.getCompensationAmount());
            item.setCreateTime(formatDate(source.getCreateTime()));
            item.setFinishTime(formatDate(source.getFinishTime()));
            item.setRemark(text(source.getRemark(), "-"));
            item.setEvents(toBrowserAfterSalesEvents(detail.getEvents()));
            item.setEvidence(toBrowserAfterSalesEvidence(detail.getEvidence()));
            result.add(item);
        }
        return result;
    }

    private List<PlatformOrderAfterSalesEventVO> toBrowserAfterSalesEvents(List<PlatformAfterSalesEventView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderAfterSalesEventVO> result = new ArrayList<PlatformOrderAfterSalesEventVO>(rows.size());
        for (PlatformAfterSalesEventView row : rows)
        {
            if (row == null) continue;
            PlatformOrderAfterSalesEventVO item = new PlatformOrderAfterSalesEventVO();
            item.setEventTypeName(text(row.getEventTypeName(), "未知事件"));
            item.setFromStatusName(row.getFromStatusName());
            item.setToStatusName(row.getToStatusName());
            item.setContent(text(row.getContent(), "-"));
            item.setOperatorName(text(row.getOperatorName(), "系统"));
            item.setCreateTime(formatDate(row.getCreateTime()));
            result.add(item);
        }
        return result;
    }

    private List<PlatformOrderAfterSalesEvidenceVO> toBrowserAfterSalesEvidence(
            List<PlatformAfterSalesEvidenceView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderAfterSalesEvidenceVO> result = new ArrayList<PlatformOrderAfterSalesEvidenceVO>(rows.size());
        for (PlatformAfterSalesEvidenceView row : rows)
        {
            if (row == null) continue;
            PlatformOrderAfterSalesEvidenceVO item = new PlatformOrderAfterSalesEvidenceVO();
            item.setEvidenceTypeName(text(row.getEvidenceTypeName(), "未知类型"));
            item.setFileUrl(row.getFileUrl());
            item.setFileName(text(row.getFileName(), "未命名文件"));
            item.setFileMime(row.getFileMime());
            item.setDescription(text(row.getDescription(), "-"));
            item.setOperatorName(text(row.getOperatorName(), "系统"));
            item.setCreateTime(formatDate(row.getCreateTime()));
            result.add(item);
        }
        return result;
    }

    private List<PlatformOrderEditLogVO> toBrowserEditLogs(List<PlatformOrderEditLogView> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderEditLogVO> result = new ArrayList<PlatformOrderEditLogVO>(rows.size());
        for (PlatformOrderEditLogView row : rows)
        {
            PlatformOrderEditLogVO item = new PlatformOrderEditLogVO();
            item.setFieldName(row.getFieldName());
            item.setBeforeDisplayValue(row.getBeforeDisplayValue());
            item.setAfterDisplayValue(row.getAfterDisplayValue());
            item.setChangeReason(row.getChangeReason());
            item.setOperatorName(row.getOperatorName());
            item.setOperatorIp(row.getOperatorIp());
            item.setCreateTime(row.getCreateTime());
            result.add(item);
        }
        return result;
    }

    private String maskPhone(String value)
    {
        String phone = value == null ? "" : value.trim();
        if (phone.isEmpty())
        {
            return "-";
        }
        if (phone.length() < 7)
        {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskAddress(String value)
    {
        String address = value == null ? "" : value.trim();
        if (address.isEmpty()) return "-";
        if (address.length() <= 8) return address;
        return address.substring(0, Math.min(8, address.length())) + "****";
    }

    private String formatDate(Date value)
    {
        return value == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    }

    private static String text(String value, String defaultValue)
    {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
