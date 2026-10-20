package com.ruoyi.merchant.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantInvoiceApplicationCreateCommand;
import com.ruoyi.merchant.model.MerchantInvoiceApplicationVO;

/** 商户开票申请数据访问。所有查询必须带当前商户范围。 */
public interface MerchantInvoiceApplicationMapper
{
    Map<String, Object> selectLedgerForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("ledgerNo") String ledgerNo);

    Map<String, Object> selectValidInvoice(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerId") Long ledgerId, @Param("refType") String refType, @Param("refId") Long refId);

    int countPendingApplication(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerId") Long ledgerId, @Param("applicationType") Integer applicationType);

    int insertApplication(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerId") Long ledgerId, @Param("applicationNo") String applicationNo,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("command") MerchantInvoiceApplicationCreateCommand command,
            @Param("amount") BigDecimal amount, @Param("refType") String refType, @Param("refId") Long refId,
            @Param("operatorId") Long operatorId);

    Long selectLastInsertId();

    MerchantInvoiceApplicationVO selectByIdempotencyKey(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("idempotencyKey") String idempotencyKey);

    MerchantInvoiceApplicationVO selectByNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("applicationNo") String applicationNo);

    List<MerchantInvoiceApplicationVO> selectList(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("status") Integer status, @Param("applicationType") Integer applicationType);
}
