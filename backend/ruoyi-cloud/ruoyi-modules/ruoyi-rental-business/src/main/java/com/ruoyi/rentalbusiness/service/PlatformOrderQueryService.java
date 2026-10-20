package com.ruoyi.rentalbusiness.service;

import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseStatus;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseType;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesEvidenceType;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesEventType;
import com.ruoyi.rentalbusiness.enums.MerchantReturnInspectionResult;
import com.ruoyi.rentalbusiness.mapper.PlatformAfterSalesMapper;
import com.ruoyi.rentalbusiness.mapper.PlatformLogisticsMapper;
import com.ruoyi.rentalbusiness.mapper.PlatformOrderMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteVO;
import com.ruoyi.system.api.model.PlatformAfterSalesDetailView;
import com.ruoyi.system.api.model.PlatformAfterSalesEventView;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceView;
import com.ruoyi.system.api.model.PlatformAfterSalesView;
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.api.model.PlatformLogisticsRouteView;
import com.ruoyi.system.api.model.PlatformLogisticsView;
import com.ruoyi.system.api.model.PlatformOrderDetailView;
import com.ruoyi.system.api.model.PlatformOrderEditLogView;
import com.ruoyi.system.api.model.PlatformOrderFulfillmentEventView;
import com.ruoyi.system.api.model.PlatformOrderItemView;
import com.ruoyi.system.api.model.PlatformOrderReturnInspectionView;
import com.ruoyi.system.api.model.PlatformOrderPageView;
import com.ruoyi.system.api.model.PlatformOrderNoteView;
import com.ruoyi.system.api.model.PlatformOrderView;

/** 平台订单只读查询服务。 */
@Service
public class PlatformOrderQueryService
{
    private static final Logger log = LoggerFactory.getLogger(PlatformOrderQueryService.class);

    private final PlatformOrderMapper orderMapper;
    private final PlatformLogisticsMapper logisticsMapper;
    private final PlatformAfterSalesMapper afterSalesMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public PlatformOrderQueryService(PlatformOrderMapper orderMapper, PlatformLogisticsMapper logisticsMapper,
            PlatformAfterSalesMapper afterSalesMapper, ObjectMapper objectMapper)
    {
        this.orderMapper = orderMapper;
        this.logisticsMapper = logisticsMapper;
        this.afterSalesMapper = afterSalesMapper;
        this.objectMapper = objectMapper;
    }

    /** 保留轻量单元测试构造方式；生产环境由 Spring 注入详情关联查询器。 */
    public PlatformOrderQueryService(PlatformOrderMapper orderMapper)
    {
        this(orderMapper, null, null, new ObjectMapper());
    }

    /** 按平台筛选条件分页查询订单，默认按创建时间倒序。 */
    public PlatformOrderPageView list(List<Long> memberIds, String keyword, Integer orderStatus, String beginTime,
            String endTime, Integer pageNum, Integer pageSize)
    {
        int normalizedPageNum = pageNum == null || pageNum.intValue() < 1 ? 1 : pageNum.intValue();
        int normalizedPageSize = pageSize == null || pageSize.intValue() < 1 ? 10
                : Math.min(pageSize.intValue(), 100);
        String normalizedKeyword = normalizeText(keyword);
        String normalizedBeginTime = normalizeDate(beginTime, "开始日期");
        String normalizedEndTime = normalizeDate(endTime, "结束日期");
        validateDateRange(normalizedBeginTime, normalizedEndTime);
        validateOrderStatus(orderStatus);
        PlatformOrderPageView page = new PlatformOrderPageView();
        if (memberIds != null && memberIds.isEmpty())
        {
            page.setRows(Collections.emptyList());
            page.setTotal(0L);
            return page;
        }
        long total = orderMapper.countPlatformOrders(memberIds, normalizedKeyword, orderStatus, normalizedBeginTime,
                normalizedEndTime);
        List<PlatformOrderView> rows = total == 0L ? Collections.emptyList()
                : orderMapper.selectPlatformOrders(memberIds, normalizedKeyword, orderStatus, normalizedBeginTime,
                        normalizedEndTime, (normalizedPageNum - 1) * normalizedPageSize, normalizedPageSize);
        page.setRows(rows);
        page.setTotal(total);
        return page;
    }

    /** 查询平台订单详情，复用订单执行面已经固化的字段和处理记录。 */
    public PlatformOrderDetailView detail(Long orderId)
    {
        if (orderId == null || orderId.longValue() <= 0)
        {
            throw new ServiceException("订单唯一编号不合法");
        }
        MerchantOrderRow row = orderMapper.selectPlatformOrderDetail(orderId);
        if (row == null)
        {
            throw new ServiceException("订单不存在");
        }
        PlatformOrderDetailView result = new PlatformOrderDetailView();
        result.setOrder(toPlatformOrderView(row));
        result.setItems(toOrderItems(row.packageDetailsJson, orderMapper.selectPlatformOrderItems(orderId)));
        PlatformOrderReturnInspectionView returnInspection = orderMapper.selectPlatformOrderReturnInspection(orderId);
        decorateReturnInspection(returnInspection);
        result.setReturnInspection(returnInspection);
        result.setLogistics(toLogistics(row.tenantId, row.memberId, orderId));
        result.setAfterSales(toAfterSales(row.tenantId, row.memberId, orderId));
        result.setNotes(toNoteViews(orderMapper.selectPlatformOrderNotes(orderId)));
        List<PlatformOrderEditLogView> editLogs = toEditLogViews(orderMapper.selectPlatformOrderEditLogs(orderId));
        result.setFulfillmentEvents(toFulfillmentEvents(editLogs));
        result.setEditLogs(editLogs);
        return result;
    }

    /** 从订单状态编辑日志组装履约时间线，保证手工和定时推进使用同一事实来源。 */
    private List<PlatformOrderFulfillmentEventView> toFulfillmentEvents(List<PlatformOrderEditLogView> editLogs)
    {
        if (editLogs == null || editLogs.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformOrderFulfillmentEventView> result = new ArrayList<PlatformOrderFulfillmentEventView>();
        for (PlatformOrderEditLogView log : editLogs)
        {
            if (log == null || !"订单状态".equals(log.getFieldName()))
            {
                continue;
            }
            PlatformOrderFulfillmentEventView event = new PlatformOrderFulfillmentEventView();
            event.setFromStatusLabel(defaultText(log.getBeforeDisplayValue(), "初始状态"));
            event.setToStatusLabel(defaultText(log.getAfterDisplayValue(), "未知状态"));
            event.setReason(defaultText(log.getChangeReason(), "订单履约状态变更"));
            event.setOperatorName(defaultText(log.getOperatorName(), "系统"));
            event.setCreateTime(log.getCreateTime());
            result.add(event);
        }
        return result;
    }

    /** 为平台订单详情补齐验机结果和售后类型中文名称。 */
    private void decorateReturnInspection(PlatformOrderReturnInspectionView inspection)
    {
        if (inspection == null)
        {
            return;
        }
        MerchantReturnInspectionResult result = MerchantReturnInspectionResult.fromCode(inspection.getResult());
        MerchantAfterSalesCaseType caseType = MerchantAfterSalesCaseType.fromCode(inspection.getCaseType());
        inspection.setResultLabel(result == null ? "未知结果" : result.getLabel());
        inspection.setCaseTypeLabel(caseType == null ? null : caseType.getLabel());
    }

    /** 优先展示订单套餐快照，避免详情页因目录变更而显示成当前商品名称。 */
    private List<PlatformOrderItemView> toOrderItems(String packageDetailsJson,
            List<PlatformOrderItemView> fallbackRows)
    {
        List<PlatformOrderItemView> snapshotRows = parseSnapshotItems(packageDetailsJson);
        if (!snapshotRows.isEmpty())
        {
            return snapshotRows;
        }
        if (fallbackRows == null || fallbackRows.isEmpty())
        {
            return Collections.emptyList();
        }
        for (PlatformOrderItemView row : fallbackRows)
        {
            if (row == null)
            {
                continue;
            }
            row.setItemTypeName("accessory".equalsIgnoreCase(row.getProductType()) ? "配件" : "产品");
            row.setItemStatusName(orderStatusLabel(row.getItemStatus(), "待处理"));
            row.setSerialNumbers(singletonIfPresent(row.getSerialNumber()));
            row.setInventoryCodes(singletonIfPresent(row.getInventoryCode()));
            if (row.getQuantity() == null || row.getQuantity().intValue() < 1)
            {
                row.setQuantity(Integer.valueOf(1));
            }
        }
        return fallbackRows;
    }

    private List<PlatformOrderItemView> parseSnapshotItems(String packageDetailsJson)
    {
        if (packageDetailsJson == null || packageDetailsJson.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        try
        {
            JsonNode root = objectMapper == null ? new ObjectMapper().readTree(packageDetailsJson)
                    : objectMapper.readTree(packageDetailsJson);
            JsonNode items = root == null || root.isArray() ? root : root.path("items");
            if (items == null || !items.isArray())
            {
                return Collections.emptyList();
            }
            List<PlatformOrderItemView> result = new ArrayList<PlatformOrderItemView>(items.size());
            for (JsonNode item : items)
            {
                if (item == null || !item.isObject())
                {
                    continue;
                }
                PlatformOrderItemView view = new PlatformOrderItemView();
                Integer itemRole = integerValue(item, "itemRole", "item_role");
                view.setItemTypeName(itemRoleLabel(itemRole));
                view.setItemName(textAny(item, "itemName", "item_name", "productName", "product_name"));
                view.setItemCode(textAny(item, "itemCode", "item_code", "productCode", "product_code",
                        "accessoryCode", "accessory_code"));
                view.setModelName(textAny(item, "itemModelName", "item_model_name", "modelName", "model_name"));
                Integer quantity = integerValue(item, "quantity");
                view.setQuantity(quantity == null || quantity.intValue() < 1 ? Integer.valueOf(1) : quantity);
                view.setSerialNumbers(jsonTextList(item, "serialNumbers", "serial_numbers"));
                if (view.getSerialNumbers().isEmpty())
                {
                    view.setSerialNumbers(singletonIfPresent(textAny(item, "serialNumber", "serial_number")));
                }
                view.setInventoryCodes(jsonTextList(item, "unitCodes", "unit_codes"));
                if (view.getInventoryCodes().isEmpty())
                {
                    view.setInventoryCodes(singletonIfPresent(textAny(item, "unitCode", "unit_code")));
                }
                view.setItemStatusName("随订单履约");
                result.add(view);
            }
            return result;
        }
        catch (JsonProcessingException exception)
        {
            log.warn("平台订单套餐快照解析失败，订单明细降级为订单商品行", exception);
            return Collections.emptyList();
        }
    }

    /** 查询订单物流单及已缓存的轨迹节点，轨迹数据不触发外部快递请求。 */
    private List<PlatformLogisticsDetailView> toLogistics(Long tenantId, Long memberId, Long orderId)
    {
        if (logisticsMapper == null || tenantId == null || memberId == null)
        {
            return Collections.emptyList();
        }
        List<PlatformLogisticsView> rows = logisticsMapper.selectLogisticsByOrder(tenantId, memberId, orderId);
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformLogisticsDetailView> result = new ArrayList<PlatformLogisticsDetailView>(rows.size());
        for (PlatformLogisticsView row : rows)
        {
            decorateLogistics(row);
            PlatformLogisticsDetailView detail = new PlatformLogisticsDetailView();
            detail.setLogistics(row);
            List<PlatformLogisticsRouteView> routes = row.getLogisticsId() == null ? Collections.emptyList()
                    : logisticsMapper.selectRoutes(tenantId, memberId, row.getLogisticsId());
            decorateLogisticsRoutes(routes);
            detail.setRoutes(routes == null ? Collections.emptyList() : routes);
            result.add(detail);
        }
        return result;
    }

    /** 查询订单关联的售后案件、处理事件和证据，不把案件编号等技术主键交给浏览器。 */
    private List<PlatformAfterSalesDetailView> toAfterSales(Long tenantId, Long memberId, Long orderId)
    {
        if (afterSalesMapper == null || tenantId == null || memberId == null)
        {
            return Collections.emptyList();
        }
        List<PlatformAfterSalesView> rows = afterSalesMapper.selectCasesByOrder(tenantId, memberId, orderId);
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformAfterSalesDetailView> result = new ArrayList<PlatformAfterSalesDetailView>(rows.size());
        for (PlatformAfterSalesView row : rows)
        {
            decorateAfterSales(row);
            PlatformAfterSalesDetailView detail = new PlatformAfterSalesDetailView();
            detail.setAfterSales(row);
            List<PlatformAfterSalesEventView> events = afterSalesMapper.selectEvents(tenantId, memberId,
                    row.getCaseId());
            List<PlatformAfterSalesEvidenceView> evidence = afterSalesMapper.selectEvidence(tenantId, memberId,
                    row.getCaseId());
            decorateAfterSalesEvents(events);
            decorateAfterSalesEvidence(evidence);
            detail.setEvents(events == null ? Collections.emptyList() : events);
            detail.setEvidence(evidence == null ? Collections.emptyList() : evidence);
            result.add(detail);
        }
        return result;
    }

    private void decorateLogistics(PlatformLogisticsView row)
    {
        if (row == null)
        {
            return;
        }
        row.setDirectionName(enumLabel(RentalCode.DeliveryDirection.values(), row.getDirection(), "未知方向"));
        row.setSourceTypeName(enumLabel(RentalCode.LogisticsSourceType.values(), row.getSourceType(), "未知来源"));
        row.setRouteStatusName(enumLabel(RentalCode.LogisticsRouteStatus.values(), row.getRouteStatus(), "未知状态"));
    }

    private void decorateLogisticsRoutes(List<PlatformLogisticsRouteView> rows)
    {
        if (rows != null)
        {
            for (PlatformLogisticsRouteView row : rows)
            {
                if (row != null)
                {
                    row.setRouteStatusName(enumLabel(RentalCode.LogisticsRouteStatus.values(), row.getRouteStatus(),
                            "未知状态"));
                }
            }
        }
    }

    private void decorateAfterSales(PlatformAfterSalesView row)
    {
        MerchantAfterSalesCaseType type = MerchantAfterSalesCaseType.fromCode(row.getCaseType());
        MerchantAfterSalesCaseStatus status = MerchantAfterSalesCaseStatus.fromCode(row.getCaseStatus());
        row.setCaseTypeName(type == null ? "未知类型" : type.getLabel());
        row.setCaseStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private void decorateAfterSalesEvents(List<PlatformAfterSalesEventView> rows)
    {
        if (rows != null)
        {
            for (PlatformAfterSalesEventView row : rows)
            {
                MerchantAfterSalesEventType type = MerchantAfterSalesEventType.fromCode(row.getEventType());
                MerchantAfterSalesCaseStatus from = MerchantAfterSalesCaseStatus.fromCode(row.getFromStatus());
                MerchantAfterSalesCaseStatus to = MerchantAfterSalesCaseStatus.fromCode(row.getToStatus());
                row.setEventTypeName(type == null ? "未知事件" : type.getLabel());
                row.setFromStatusName(from == null ? null : from.getLabel());
                row.setToStatusName(to == null ? null : to.getLabel());
            }
        }
    }

    private void decorateAfterSalesEvidence(List<PlatformAfterSalesEvidenceView> rows)
    {
        if (rows != null)
        {
            for (PlatformAfterSalesEvidenceView row : rows)
            {
                MerchantAfterSalesEvidenceType type = MerchantAfterSalesEvidenceType.fromCode(row.getEvidenceType());
                row.setEvidenceTypeName(type == null ? "未知类型" : type.getLabel());
            }
        }
    }

    private PlatformOrderView toPlatformOrderView(MerchantOrderRow row)
    {
        PlatformOrderView view = new PlatformOrderView();
        view.setOrderId(row.orderId);
        view.setOrderNo(row.orderNo);
        view.setTenantId(row.tenantId);
        view.setMemberId(row.memberId);
        view.setSourceShopName(row.shopName);
        view.setCustomerName(row.customerName);
        view.setCustomerPhone(row.customerPhone);
        view.setRecipientName(row.recipientName);
        view.setRecipientPhone(row.recipientPhone);
        view.setLeaseStartDate(formatDate(row.leaseStartDate, "yyyy-MM-dd"));
        view.setLeaseEndDate(formatDate(row.leaseEndDate, "yyyy-MM-dd"));
        view.setRentAmount(row.rentAmount);
        view.setDepositAmount(row.depositAmount);
        view.setOrderStatus(row.orderStatus);
        view.setCreateTime(formatDate(row.createTime, "yyyy-MM-dd HH:mm:ss"));
        view.setExternalOrderNo(row.externalOrderNo);
        view.setRecipientAddress(row.recipientAddress);
        view.setCustomerNote(row.customerNote);
        view.setWarehouseName(row.warehouseName);
        view.setReturnWarehouseName(row.returnWarehouseName);
        view.setWarehouseCity(row.warehouseCity);
        view.setProductName(row.productName);
        view.setModelName(row.modelName);
        view.setProductCode(row.productCode);
        view.setPackageName(row.packageName);
        view.setPackageDetailsJson(row.packageDetailsJson);
        view.setShippingCarrier(row.shippingCarrier);
        view.setShippingTrackingNo(row.shippingTrackingNo);
        view.setReturnCarrier(row.returnCarrier);
        view.setReturnTrackingNo(row.returnTrackingNo);
        view.setShipDueTime(formatDate(row.shipDueTime, "yyyy-MM-dd HH:mm:ss"));
        view.setReturnDueTime(formatDate(row.returnDueTime, "yyyy-MM-dd HH:mm:ss"));
        view.setArrivalDate(formatDate(row.arrivalDate, "yyyy-MM-dd"));
        view.setDeliveryAmount(row.deliveryAmount);
        view.setInsuranceAmount(row.insuranceAmount);
        view.setLeaseDays(row.leaseDays);
        view.setPayStatus(row.payStatus);
        view.setTimeEffectStatus(row.timeEffectStatus);
        view.setPriorityFlag(row.priorityFlag);
        view.setWeatherRiskFlag(row.weatherRiskFlag);
        view.setRemark(row.remark);
        return view;
    }

    private List<PlatformOrderNoteView> toNoteViews(List<MerchantOrderNoteVO> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderNoteView> result = new ArrayList<PlatformOrderNoteView>(rows.size());
        for (MerchantOrderNoteVO row : rows)
        {
            PlatformOrderNoteView view = new PlatformOrderNoteView();
            view.setNoteContent(row.noteContent);
            view.setNoteType(row.noteType);
            view.setOperatorName(row.operatorName);
            view.setCreateTime(row.createTime);
            result.add(view);
        }
        return result;
    }

    private List<PlatformOrderEditLogView> toEditLogViews(List<MerchantOrderEditLogVO> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<PlatformOrderEditLogView> result = new ArrayList<PlatformOrderEditLogView>(rows.size());
        for (MerchantOrderEditLogVO row : rows)
        {
            PlatformOrderEditLogView view = new PlatformOrderEditLogView();
            view.setFieldName(row.fieldName);
            view.setBeforeDisplayValue(row.beforeDisplayValue);
            view.setAfterDisplayValue(row.afterDisplayValue);
            view.setChangeReason(row.changeReason);
            view.setOperatorName(row.operatorName);
            view.setOperatorIp(row.operatorIp);
            view.setCreateTime(formatDate(row.createTime, "yyyy-MM-dd HH:mm:ss"));
            result.add(view);
        }
        return result;
    }

    private String itemRoleLabel(Integer itemRole)
    {
        if (Integer.valueOf(1).equals(itemRole) || Integer.valueOf(4).equals(itemRole))
        {
            return "产品";
        }
        if (Integer.valueOf(2).equals(itemRole) || Integer.valueOf(3).equals(itemRole))
        {
            return "配件";
        }
        return "套餐明细";
    }

    private String orderStatusLabel(Integer status, String fallback)
    {
        if (status != null)
        {
            for (RentalCode.OrderStatus value : RentalCode.OrderStatus.values())
            {
                if (value.getCode().equals(status))
                {
                    return value.getLabel();
                }
            }
        }
        return fallback;
    }

    private <T extends com.ruoyi.common.core.enums.NumericCode> String enumLabel(T[] values, Integer code,
            String fallback)
    {
        if (code != null)
        {
            for (T value : values)
            {
                if (value.getCode().equals(code))
                {
                    return value.getLabel();
                }
            }
        }
        return fallback;
    }

    private Integer integerValue(JsonNode node, String... fieldNames)
    {
        String text = textAny(node, fieldNames);
        if (text == null)
        {
            return null;
        }
        try
        {
            return Integer.valueOf(text);
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    private String textAny(JsonNode node, String... fieldNames)
    {
        if (node == null || !node.isObject() || fieldNames == null)
        {
            return null;
        }
        for (String fieldName : fieldNames)
        {
            JsonNode value = node.get(fieldName);
            if (value != null && !value.isNull() && !value.isMissingNode())
            {
                String text = value.asText();
                if (text != null && !text.trim().isEmpty())
                {
                    return text.trim();
                }
            }
        }
        return null;
    }

    private List<String> jsonTextList(JsonNode node, String... fieldNames)
    {
        if (node == null || !node.isObject() || fieldNames == null)
        {
            return new ArrayList<>();
        }
        for (String fieldName : fieldNames)
        {
            JsonNode values = node.get(fieldName);
            if (values == null || !values.isArray())
            {
                continue;
            }
            List<String> result = new ArrayList<String>(values.size());
            for (JsonNode value : values)
            {
                if (value != null && !value.isNull() && !value.asText().trim().isEmpty())
                {
                    result.add(value.asText().trim());
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    private List<String> singletonIfPresent(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<String>(1);
        result.add(value.trim());
        return result;
    }

    private String formatDate(Date value, String pattern)
    {
        return value == null ? null : new SimpleDateFormat(pattern).format(value);
    }

    private String normalizeText(String value)
    {
        if (value == null)
        {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String defaultText(String value, String fallback)
    {
        String normalized = normalizeText(value);
        return normalized == null ? fallback : normalized;
    }

    private String normalizeDate(String value, String fieldName)
    {
        String normalized = normalizeText(value);
        if (normalized == null)
        {
            return null;
        }
        try
        {
            LocalDate.parse(normalized);
            return normalized;
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException(fieldName + "格式不正确，应为 yyyy-MM-dd");
        }
    }

    private void validateDateRange(String beginTime, String endTime)
    {
        if (beginTime != null && endTime != null && LocalDate.parse(beginTime).isAfter(LocalDate.parse(endTime)))
        {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
    }

    private void validateOrderStatus(Integer orderStatus)
    {
        if (orderStatus == null)
        {
            return;
        }
        for (RentalCode.OrderStatus status : RentalCode.OrderStatus.values())
        {
            if (status.getCode().equals(orderStatus))
            {
                return;
            }
        }
        throw new ServiceException("订单状态不合法");
    }
}
