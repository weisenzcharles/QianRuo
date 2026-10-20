package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseStatus;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseType;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesEvidenceType;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesEventType;
import com.ruoyi.rentalbusiness.mapper.PlatformAfterSalesMapper;
import com.ruoyi.system.api.model.PlatformAfterSalesDetailView;
import com.ruoyi.system.api.model.PlatformAfterSalesEventCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesEventView;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceView;
import com.ruoyi.system.api.model.PlatformAfterSalesPageView;
import com.ruoyi.system.api.model.PlatformAfterSalesQuery;
import com.ruoyi.system.api.model.PlatformAfterSalesView;

/** 平台售后案件应用服务，统一处理查询、事件追加和订单完成联动。 */
@Service
public class PlatformAfterSalesService
{
    private final PlatformAfterSalesMapper mapper;

    public PlatformAfterSalesService(PlatformAfterSalesMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 分页查询跨商户售后案件，并为状态、类型补齐中文名称。 */
    public PlatformAfterSalesPageView list(PlatformAfterSalesQuery query)
    {
        PlatformAfterSalesQuery normalized = query == null ? new PlatformAfterSalesQuery() : query;
        normalized.setKeyword(trim(normalized.getKeyword()));
        normalized.setMerchantKeyword(trim(normalized.getMerchantKeyword()));
        if (normalized.getCaseStatus() != null
                && MerchantAfterSalesCaseStatus.fromCode(normalized.getCaseStatus()) == null)
        {
            throw new ServiceException("售后状态不合法");
        }
        int pageNum = normalized.getPageNum() == null || normalized.getPageNum().intValue() < 1
                ? 1 : normalized.getPageNum().intValue();
        int pageSize = normalized.getPageSize() == null || normalized.getPageSize().intValue() < 1
                ? 10 : Math.min(normalized.getPageSize().intValue(), 100);
        normalized.setPageNum(pageNum);
        normalized.setPageSize(pageSize);

        PlatformAfterSalesPageView result = new PlatformAfterSalesPageView();
        long total = mapper.countCases(normalized);
        List<PlatformAfterSalesView> rows = total == 0L ? Collections.emptyList()
                : mapper.selectCases(normalized, (pageNum - 1) * pageSize, pageSize);
        decorateCases(rows);
        result.setRows(rows == null ? Collections.emptyList() : rows);
        result.setTotal(total);
        return result;
    }

    /** 查询售后案件详情、处理时间线和证据。 */
    public PlatformAfterSalesDetailView detail(Long caseId)
    {
        requirePositive(caseId, "售后唯一编号不能为空");
        PlatformAfterSalesView row = mapper.selectCase(caseId);
        if (row == null)
        {
            throw new ServiceException("售后案件不存在");
        }
        decorateCase(row);
        PlatformAfterSalesDetailView result = new PlatformAfterSalesDetailView();
        result.setAfterSales(row);
        List<PlatformAfterSalesEventView> events = mapper.selectEvents(row.getTenantId(), row.getMemberId(), caseId);
        List<PlatformAfterSalesEvidenceView> evidence = mapper.selectEvidence(row.getTenantId(), row.getMemberId(), caseId);
        decorateEvents(events);
        decorateEvidence(evidence);
        result.setEvents(events == null ? Collections.emptyList() : events);
        result.setEvidence(evidence == null ? Collections.emptyList() : evidence);
        return result;
    }

    /** 追加平台处理事件，并在最后一个售后案件完成时推进原订单。 */
    @Transactional(rollbackFor = Exception.class)
    public void appendEvent(Long caseId, PlatformAfterSalesEventCommand command, Long operatorId,
            String operatorName)
    {
        requirePositive(caseId, "售后唯一编号不能为空");
        requirePositive(operatorId, "平台操作人不能为空");
        if (command == null || StringUtils.isBlank(command.getContent()))
        {
            throw new ServiceException("处理说明不能为空");
        }
        PlatformAfterSalesView before = mapper.selectCaseForUpdate(caseId);
        if (before == null)
        {
            throw new ServiceException("售后案件不存在");
        }
        Integer currentStatus = before.getCaseStatus();
        MerchantAfterSalesCaseStatus current = MerchantAfterSalesCaseStatus.fromCode(currentStatus);
        if (current == null)
        {
            throw new ServiceException("当前售后状态不合法");
        }
        if (current != MerchantAfterSalesCaseStatus.PROCESSING)
        {
            throw new ServiceException("已完成或已关闭的售后案件不能继续处理");
        }

        Integer targetStatus = command.getToStatus() == null ? currentStatus : command.getToStatus();
        MerchantAfterSalesCaseStatus target = MerchantAfterSalesCaseStatus.fromCode(targetStatus);
        if (target == null)
        {
            throw new ServiceException("目标售后状态不合法");
        }
        String content = trimToLength(command.getContent(), 2000);
        String actor = StringUtils.isBlank(operatorName) ? "平台用户" : trimToLength(operatorName, 128);
        if (!currentStatus.equals(targetStatus)
                && mapper.updateCaseStatus(before.getTenantId(), before.getMemberId(), caseId,
                        currentStatus, targetStatus, operatorId, content) != 1)
        {
            throw new ServiceException("售后状态已变化，请刷新后重试");
        }
        MerchantAfterSalesEventType eventType = target == MerchantAfterSalesCaseStatus.COMPLETED
                ? MerchantAfterSalesEventType.COMPLETED : MerchantAfterSalesEventType.NOTE;
        if (mapper.insertEvent(before.getTenantId(), before.getMemberId(), caseId, eventType.getCode(),
                currentStatus, targetStatus, content, operatorId, actor) != 1)
        {
            throw new ServiceException("售后处理记录保存失败");
        }
        if (target == MerchantAfterSalesCaseStatus.COMPLETED
                && mapper.countProcessingByOrder(before.getTenantId(), before.getMemberId(), before.getOrderId()) == 0)
        {
            completeOrderIfNeeded(before, operatorId, actor, content);
        }
    }

    /** 追加平台售后证据，并将证据写入案件时间线。 */
    @Transactional(rollbackFor = Exception.class)
    public void addEvidence(Long caseId, PlatformAfterSalesEvidenceCommand command, Long operatorId,
            String operatorName)
    {
        requirePositive(caseId, "售后唯一编号不能为空");
        requirePositive(operatorId, "平台操作人不能为空");
        if (command == null || MerchantAfterSalesEvidenceType.fromCode(command.getEvidenceType()) == null)
        {
            throw new ServiceException("请选择证据类型");
        }
        if (StringUtils.isBlank(command.getFileUrl()))
        {
            throw new ServiceException("请上传证据文件");
        }
        PlatformAfterSalesView before = mapper.selectCaseForUpdate(caseId);
        if (before == null)
        {
            throw new ServiceException("售后案件不存在");
        }
        MerchantAfterSalesCaseStatus current = MerchantAfterSalesCaseStatus.fromCode(before.getCaseStatus());
        if (current != MerchantAfterSalesCaseStatus.PROCESSING)
        {
            throw new ServiceException("已完成或已关闭的售后案件不能继续处理");
        }

        String fileUrl = trimToLength(command.getFileUrl(), 1000);
        String fileName = trimToLength(command.getFileName(), 255);
        if (StringUtils.isBlank(fileName))
        {
            fileName = extractFileName(fileUrl);
        }
        String fileMime = trimToLength(command.getFileMime(), 128);
        String description = trimToLength(command.getDescription(), 1000);
        String actor = StringUtils.isBlank(operatorName) ? "平台用户" : trimToLength(operatorName, 128);
        if (mapper.insertEvidence(before.getTenantId(), before.getMemberId(), caseId, command.getEvidenceType(),
                fileUrl, fileName, fileMime, description, operatorId, actor) != 1)
        {
            throw new ServiceException("售后证据保存失败");
        }
        String eventContent = StringUtils.isBlank(description) ? fileName : description;
        if (mapper.insertEvent(before.getTenantId(), before.getMemberId(), caseId,
                MerchantAfterSalesEventType.EVIDENCE_ADDED.getCode(), null, null,
                trimToLength(eventContent, 2000), operatorId, actor) != 1)
        {
            throw new ServiceException("售后证据流水保存失败");
        }
    }

    private void completeOrderIfNeeded(PlatformAfterSalesView afterSales, Long operatorId, String operatorName,
            String reason)
    {
        Integer fromStatus = RentalCode.OrderStatus.AFTER_SALES.getCode();
        Integer toStatus = RentalCode.OrderStatus.COMPLETED.getCode();
        int updated = mapper.updateOrderStatus(afterSales.getTenantId(), afterSales.getMemberId(),
                afterSales.getOrderId(), fromStatus, toStatus, operatorId);
        if (updated == 1)
        {
            mapper.insertOrderEditLog(afterSales.getTenantId(), afterSales.getMemberId(), afterSales.getOrderId(),
                    "订单状态", String.valueOf(fromStatus), String.valueOf(toStatus),
                    RentalCode.OrderStatus.AFTER_SALES.getLabel(), RentalCode.OrderStatus.COMPLETED.getLabel(),
                    StringUtils.isBlank(reason) ? "平台售后处理完成" : trimToLength(reason, 500), operatorId,
                    operatorName);
        }
    }

    private void decorateCases(List<PlatformAfterSalesView> rows)
    {
        if (rows != null)
        {
            for (PlatformAfterSalesView row : rows)
            {
                decorateCase(row);
            }
        }
    }

    private void decorateCase(PlatformAfterSalesView row)
    {
        if (row == null)
        {
            return;
        }
        MerchantAfterSalesCaseType type = MerchantAfterSalesCaseType.fromCode(row.getCaseType());
        MerchantAfterSalesCaseStatus status = MerchantAfterSalesCaseStatus.fromCode(row.getCaseStatus());
        row.setCaseTypeName(type == null ? "未知类型" : type.getLabel());
        row.setCaseStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private void decorateEvents(List<PlatformAfterSalesEventView> rows)
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

    private void decorateEvidence(List<PlatformAfterSalesEvidenceView> rows)
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

    private static String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    private static String trimToLength(String value, int maxLength)
    {
        String normalized = trim(value);
        if (normalized == null || normalized.length() <= maxLength)
        {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    private static String extractFileName(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return "售后证据";
        }
        int queryIndex = fileUrl.indexOf('?');
        String path = queryIndex >= 0 ? fileUrl.substring(0, queryIndex) : fileUrl;
        int slashIndex = path.lastIndexOf('/');
        String fileName = slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
        return StringUtils.isBlank(fileName) ? "售后证据" : trimToLength(fileName, 255);
    }

    private static void requirePositive(Long value, String message)
    {
        if (value == null || value.longValue() <= 0L)
        {
            throw new ServiceException(message);
        }
    }
}
