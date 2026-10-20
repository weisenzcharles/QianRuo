package com.ruoyi.system.mapper.rental;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationQuery;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationVO;

/** 平台发票申请、开具和红冲数据访问组件。 */
public interface PlatformInvoiceApplicationMapper
{
    List<PlatformInvoiceApplicationVO> selectList(@Param("query") PlatformInvoiceApplicationQuery query);

    PlatformInvoiceApplicationVO selectByNo(@Param("applicationNo") String applicationNo);

    Map<String, Object> selectApplicationForUpdate(@Param("applicationNo") String applicationNo);

    Map<String, Object> selectLedgerForUpdate(@Param("ledgerId") Long ledgerId);

    Map<String, Object> selectValidInvoice(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerId") Long ledgerId, @Param("refType") String refType, @Param("refId") Long refId);

    Map<String, Object> selectInvoiceByIdForUpdate(@Param("invoiceRecordId") Long invoiceRecordId);

    PlatformInvoiceApplicationVO selectByIdempotencyKey(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("idempotencyKey") String idempotencyKey);

    int insertApplication(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerId") Long ledgerId, @Param("applicationNo") String applicationNo,
            @Param("idempotencyKey") String idempotencyKey, @Param("applicationType") Integer applicationType,
            @Param("applicationStatus") Integer applicationStatus, @Param("invoiceType") Integer invoiceType,
            @Param("invoiceTitle") String invoiceTitle, @Param("taxNo") String taxNo,
            @Param("invoiceEmail") String invoiceEmail, @Param("amount") java.math.BigDecimal amount,
            @Param("refType") String refType, @Param("refId") Long refId,
            @Param("originalApplicationId") Long originalApplicationId,
            @Param("originalInvoiceRecordId") Long originalInvoiceRecordId,
            @Param("reviewRemark") String reviewRemark, @Param("operatorId") Long operatorId);

    int updateApplicationStatus(@Param("applicationId") Long applicationId,
            @Param("applicationStatus") Integer applicationStatus, @Param("reviewRemark") String reviewRemark,
            @Param("reviewerId") Long reviewerId);

    int insertInvoiceRecord(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("applicationId") Long applicationId, @Param("ledgerId") Long ledgerId,
            @Param("invoiceNo") String invoiceNo, @Param("invoiceCode") String invoiceCode,
            @Param("fileUrl") String fileUrl, @Param("operatorId") Long operatorId);

    int insertRedFlushRecord(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("originalInvoiceRecordId") Long originalInvoiceRecordId,
            @Param("applicationId") Long applicationId, @Param("redFlushNo") String redFlushNo,
            @Param("reason") String reason, @Param("reviewerId") Long reviewerId);

    int updateInvoiceRecordStatus(@Param("invoiceRecordId") Long invoiceRecordId,
            @Param("recordStatus") Integer recordStatus, @Param("operatorId") Long operatorId);

    int updateLedgerInvoiceStatus(@Param("ledgerId") Long ledgerId, @Param("invoiceStatus") Integer invoiceStatus,
            @Param("operatorId") Long operatorId);
}
