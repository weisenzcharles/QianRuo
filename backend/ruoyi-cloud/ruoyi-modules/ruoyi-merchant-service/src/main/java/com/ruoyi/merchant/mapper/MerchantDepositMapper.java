package com.ruoyi.merchant.mapper;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 商户保证金账户与流水数据访问接口。 */
public interface MerchantDepositMapper
{
    Map<String, Object> selectAccountForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("accountType") Integer accountType);

    int insertAccount(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountType") Integer accountType, @Param("operatorId") Long operatorId);

    Map<String, Object> selectLedgerByKey(@Param("tenantId") Long tenantId,
            @Param("idempotencyKey") String idempotencyKey);

    int updateBalance(@Param("accountId") Long accountId, @Param("availableAmount") BigDecimal availableAmount,
            @Param("frozenAmount") BigDecimal frozenAmount, @Param("version") Integer version,
            @Param("operatorId") Long operatorId);

    int insertLedger(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("memberId") Long memberId, @Param("direction") Integer direction,
            @Param("amount") BigDecimal amount, @Param("refType") String refType, @Param("refId") Long refId,
            @Param("balanceAfter") BigDecimal balanceAfter, @Param("idempotencyKey") String idempotencyKey,
            @Param("operatorId") Long operatorId, @Param("remark") String remark);

    int insertFinanceLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountType") Integer accountType, @Param("ledgerNo") String ledgerNo,
            @Param("direction") Integer direction, @Param("bizType") String bizType,
            @Param("amount") BigDecimal amount, @Param("balanceAfter") BigDecimal balanceAfter,
            @Param("refType") String refType, @Param("refId") Long refId,
            @Param("payOrderNo") String payOrderNo, @Param("operatorId") Long operatorId,
            @Param("remark") String remark);
}
