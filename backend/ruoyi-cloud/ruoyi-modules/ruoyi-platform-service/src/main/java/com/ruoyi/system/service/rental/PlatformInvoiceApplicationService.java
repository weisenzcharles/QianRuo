package com.ruoyi.system.service.rental;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.mapper.rental.PlatformInvoiceApplicationMapper;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationQuery;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationStatus;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationType;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationVO;
import com.ruoyi.system.model.rental.PlatformInvoiceRedFlushCommand;
import com.ruoyi.system.model.rental.PlatformInvoiceReviewCommand;

/** 平台发票治理服务，负责审核、开具和红冲的状态闭环。 */
@Service
public class PlatformInvoiceApplicationService
{
    private static final DateTimeFormatter APPLICATION_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final PlatformInvoiceApplicationMapper mapper;

    public PlatformInvoiceApplicationService(PlatformInvoiceApplicationMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 分页查询开票申请。 */
    public List<PlatformInvoiceApplicationVO> list(PlatformInvoiceApplicationQuery query)
    {
        if (query == null)
        {
            query = new PlatformInvoiceApplicationQuery();
        }
        normalize(query);
        if (query.getApplicationStatus() != null
                && PlatformInvoiceApplicationStatus.fromCode(query.getApplicationStatus()) == null)
        {
            throw new ServiceException("不支持的发票申请状态");
        }
        if (query.getApplicationType() != null
                && PlatformInvoiceApplicationType.fromCode(query.getApplicationType()) == null)
        {
            throw new ServiceException("不支持的发票申请类型");
        }
        List<PlatformInvoiceApplicationVO> rows = mapper.selectList(query);
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 查询一笔开票申请详情。 */
    public PlatformInvoiceApplicationVO get(String applicationNo)
    {
        String normalizedNo = trim(applicationNo);
        return normalizedNo == null ? null : mapper.selectByNo(normalizedNo);
    }

    /** 审核开票申请；已填写发票号码时直接完成开具，否则进入待开具。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformInvoiceApplicationVO review(String applicationNo, PlatformInvoiceReviewCommand command)
    {
        String normalizedNo = trim(applicationNo);
        validateReview(command);
        MapValue application = lockApplication(normalizedNo);
        requireStatus(application, PlatformInvoiceApplicationStatus.PENDING_REVIEW);
        Long operatorId = SecurityUtils.getUserId();
        Integer applicationType = application.integer("applicationType");
        if (!command.getApproved().booleanValue())
        {
            reject(application, trim(command.getReviewRemark()), operatorId);
            return get(normalizedNo);
        }

        if (PlatformInvoiceApplicationType.RED_FLUSH.getCode() == applicationType.intValue())
        {
            completeRedFlush(application, command, operatorId);
        }
        else if (hasText(command.getInvoiceNo()))
        {
            issue(application, command.getInvoiceNo(), command.getInvoiceCode(), command.getInvoiceFileUrl(), operatorId);
        }
        else
        {
            if (mapper.updateApplicationStatus(application.longValue("applicationId"),
                    PlatformInvoiceApplicationStatus.PENDING_ISSUE.getCode(), trim(command.getReviewRemark()),
                    operatorId) != 1)
            {
                throw new ServiceException("开票申请状态更新失败");
            }
            mapper.updateLedgerInvoiceStatus(application.longValue("ledgerId"), 0, operatorId);
        }
        return get(normalizedNo);
    }

    /** 为待开具申请补录发票信息并完成开具。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformInvoiceApplicationVO issue(String applicationNo, PlatformInvoiceReviewCommand command)
    {
        String normalizedNo = trim(applicationNo);
        if (command == null || !hasText(command.getInvoiceNo()))
        {
            throw new ServiceException("请填写发票号码");
        }
        MapValue application = lockApplication(normalizedNo);
        requireStatus(application, PlatformInvoiceApplicationStatus.PENDING_ISSUE);
        if (PlatformInvoiceApplicationType.RED_FLUSH.getCode() == application.integer("applicationType").intValue())
        {
            throw new ServiceException("红冲申请不能使用开具操作");
        }
        issue(application, command.getInvoiceNo(), command.getInvoiceCode(), command.getInvoiceFileUrl(),
                SecurityUtils.getUserId());
        return get(normalizedNo);
    }

    /** 为已开具发票创建红冲申请，红冲仍需平台审核。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformInvoiceApplicationVO createRedFlush(String originalApplicationNo,
            PlatformInvoiceRedFlushCommand command)
    {
        if (trim(originalApplicationNo) == null || command == null || !hasText(command.getReason()))
        {
            throw new ServiceException("红冲申请参数不完整");
        }
        String reason = command.getReason().trim();
        if (reason.length() > 500)
        {
            throw new ServiceException("红冲原因不能超过500个字符");
        }
        String idempotencyKey = hasText(command.getIdempotencyKey()) ? command.getIdempotencyKey().trim()
                : UUID.randomUUID().toString();
        if (idempotencyKey.length() > 128)
        {
            throw new ServiceException("幂等请求标识不能超过128个字符");
        }
        MapValue original = lockApplication(originalApplicationNo.trim());
        requireStatus(original, PlatformInvoiceApplicationStatus.ISSUED);
        Map<String, Object> invoice = mapper.selectInvoiceByIdForUpdate(original.longValue("invoiceRecordId"));
        if (invoice == null || !Integer.valueOf(1).equals(integer(invoice.get("recordStatus"))))
        {
            throw new ServiceException("原发票不存在或已红冲");
        }
        PlatformInvoiceApplicationVO existing = mapper.selectByIdempotencyKey(original.longValue("tenantId"),
                original.longValue("memberId"), idempotencyKey);
        if (existing != null)
        {
            return existing;
        }
        String applicationNo = buildNo("RDF-");
        Long operatorId = SecurityUtils.getUserId();
        try
        {
            mapper.insertApplication(original.longValue("tenantId"), original.longValue("memberId"),
                    original.longValue("ledgerId"), applicationNo, idempotencyKey,
                    PlatformInvoiceApplicationType.RED_FLUSH.getCode(),
                    PlatformInvoiceApplicationStatus.PENDING_REVIEW.getCode(), original.integer("invoiceType"),
                    original.string("invoiceTitle"), original.string("taxNo"), original.string("invoiceEmail"),
                    original.decimal("amount"), original.string("refType"), original.longValue("refId"),
                    original.longValue("applicationId"), original.longValue("invoiceRecordId"), reason, operatorId);
        }
        catch (DuplicateKeyException exception)
        {
            PlatformInvoiceApplicationVO retry = mapper.selectByIdempotencyKey(original.longValue("tenantId"),
                    original.longValue("memberId"), idempotencyKey);
            if (retry != null)
            {
                return retry;
            }
            throw new ServiceException("红冲申请已提交，请刷新后重试");
        }
        PlatformInvoiceApplicationVO result = get(applicationNo);
        if (result == null)
        {
            throw new ServiceException("红冲申请保存失败");
        }
        return result;
    }

    private void completeRedFlush(MapValue application, PlatformInvoiceReviewCommand command, Long operatorId)
    {
        Long invoiceRecordId = application.longValue("originalInvoiceRecordId");
        Map<String, Object> invoice = mapper.selectInvoiceByIdForUpdate(invoiceRecordId);
        if (invoice == null || !Integer.valueOf(1).equals(integer(invoice.get("recordStatus"))))
        {
            throw new ServiceException("原发票不存在或已红冲");
        }
        String redFlushNo = hasText(command.getInvoiceNo()) ? command.getInvoiceNo().trim() : buildNo("RF-");
        String reason = hasText(application.string("reviewRemark"))
                ? application.string("reviewRemark") : trim(command.getReviewRemark());
        if (mapper.insertRedFlushRecord(application.longValue("tenantId"), application.longValue("memberId"),
                invoiceRecordId, application.longValue("applicationId"), redFlushNo,
                reason, operatorId) != 1)
        {
            throw new ServiceException("红冲记录保存失败");
        }
        if (mapper.updateInvoiceRecordStatus(invoiceRecordId, 2, operatorId) != 1
                || mapper.updateApplicationStatus(application.longValue("applicationId"),
                        PlatformInvoiceApplicationStatus.RED_FLUSHED.getCode(), trim(command.getReviewRemark()),
                        operatorId) != 1
                || mapper.updateLedgerInvoiceStatus(application.longValue("ledgerId"), 3, operatorId) != 1)
        {
            throw new ServiceException("红冲状态更新失败");
        }
    }

    private void issue(MapValue application, String invoiceNo, String invoiceCode, String fileUrl, Long operatorId)
    {
        String normalizedInvoiceNo = trim(invoiceNo);
        if (normalizedInvoiceNo == null || normalizedInvoiceNo.length() > 128)
        {
            throw new ServiceException("发票号码不能为空且不能超过128个字符");
        }
        if (trim(invoiceCode) != null && invoiceCode.trim().length() > 64)
        {
            throw new ServiceException("发票代码不能超过64个字符");
        }
        if (trim(fileUrl) != null && fileUrl.trim().length() > 500)
        {
            throw new ServiceException("发票文件地址不能超过500个字符");
        }
        Map<String, Object> ledger = mapper.selectLedgerForUpdate(application.longValue("ledgerId"));
        if (ledger == null)
        {
            throw new ServiceException("财务流水不存在");
        }
        if (mapper.selectValidInvoice(application.longValue("tenantId"), application.longValue("memberId"),
                application.longValue("ledgerId"), application.string("refType"), application.longValue("refId")) != null)
        {
            throw new ServiceException("该业务已经存在有效发票，不能重复开票");
        }
        if (mapper.insertInvoiceRecord(application.longValue("tenantId"), application.longValue("memberId"),
                application.longValue("applicationId"), application.longValue("ledgerId"), normalizedInvoiceNo,
                trim(invoiceCode), trim(fileUrl), operatorId) != 1
                || mapper.updateApplicationStatus(application.longValue("applicationId"),
                        PlatformInvoiceApplicationStatus.ISSUED.getCode(), null, operatorId) != 1
                || mapper.updateLedgerInvoiceStatus(application.longValue("ledgerId"), 1, operatorId) != 1)
        {
            throw new ServiceException("发票开具状态保存失败");
        }
    }

    private void reject(MapValue application, String reviewRemark, Long operatorId)
    {
        if (!hasText(reviewRemark))
        {
            throw new ServiceException("驳回申请时必须填写审核备注");
        }
        if (mapper.updateApplicationStatus(application.longValue("applicationId"),
                PlatformInvoiceApplicationStatus.REJECTED.getCode(), reviewRemark, operatorId) != 1)
        {
            throw new ServiceException("发票申请驳回失败");
        }
        if (PlatformInvoiceApplicationType.RED_FLUSH.getCode() != application.integer("applicationType").intValue())
        {
            mapper.updateLedgerInvoiceStatus(application.longValue("ledgerId"), 2, operatorId);
        }
    }

    private MapValue lockApplication(String applicationNo)
    {
        if (applicationNo == null || applicationNo.isBlank())
        {
            throw new ServiceException("开票申请号不能为空");
        }
        MapValue application = new MapValue(mapper.selectApplicationForUpdate(applicationNo.trim()));
        if (application.value == null)
        {
            throw new ServiceException("开票申请不存在或已删除");
        }
        return application;
    }

    private void requireStatus(MapValue application, PlatformInvoiceApplicationStatus expected)
    {
        if (!Integer.valueOf(expected.getCode()).equals(application.integer("applicationStatus")))
        {
            throw new ServiceException("当前申请状态不允许此操作");
        }
    }

    private void validateReview(PlatformInvoiceReviewCommand command)
    {
        if (command == null || command.getApproved() == null)
        {
            throw new ServiceException("发票审核参数不完整");
        }
        if (hasText(command.getInvoiceNo()) && command.getInvoiceNo().trim().length() > 128)
        {
            throw new ServiceException("发票号码不能超过128个字符");
        }
    }

    private void normalize(PlatformInvoiceApplicationQuery query)
    {
        query.setApplicationNo(trim(query.getApplicationNo()));
        query.setMerchantKeyword(trim(query.getMerchantKeyword()));
        query.setBeginTime(trim(query.getBeginTime()));
        query.setEndTime(trim(query.getEndTime()));
    }

    private String buildNo(String prefix)
    {
        return prefix + LocalDateTime.now().format(APPLICATION_TIME) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    private Integer integer(Object value)
    {
        return value == null ? null : ((Number) value).intValue();
    }

    private static final class MapValue
    {
        private final Map<String, Object> value;

        private MapValue(Map<String, Object> value)
        {
            this.value = value;
        }

        private Integer integer(String key)
        {
            return value.get(key) == null ? null : ((Number) value.get(key)).intValue();
        }

        private Long longValue(String key)
        {
            return value.get(key) == null ? null : ((Number) value.get(key)).longValue();
        }

        private BigDecimal decimal(String key)
        {
            return value.get(key) == null ? BigDecimal.ZERO : new BigDecimal(value.get(key).toString());
        }

        private String string(String key)
        {
            return value.get(key) == null ? null : value.get(key).toString();
        }
    }
}
