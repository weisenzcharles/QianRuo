package com.ruoyi.merchant.mapper;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 周转市场保证金与履约保证金账户投影数据访问。 */
public interface MerchantMarketDepositMapper
{
    Map<String, Object> selectAccountForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("accountType") Integer accountType);

    int insertAccount(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountType") Integer accountType, @Param("operatorId") Long operatorId);

    int updateAccount(@Param("accountId") Long accountId, @Param("availableAmount") BigDecimal availableAmount,
            @Param("frozenAmount") BigDecimal frozenAmount, @Param("version") Long version,
            @Param("operatorId") Long operatorId);

    Map<String, Object> selectLatestDeposit(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("postId") Long postId);

    int insertFinanceLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountType") Integer accountType, @Param("ledgerNo") String ledgerNo,
            @Param("direction") Integer direction, @Param("bizType") String bizType,
            @Param("amount") BigDecimal amount, @Param("balanceAfter") BigDecimal balanceAfter,
            @Param("refId") Long refId, @Param("refType") String refType,
            @Param("payChannel") String payChannel, @Param("payOrderNo") String payOrderNo,
            @Param("operatorId") Long operatorId, @Param("remark") String remark);

    int insertMarketDepositLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("amount") BigDecimal amount, @Param("direction") Integer direction,
            @Param("refType") String refType, @Param("refId") Long refId,
            @Param("idempotencyKey") String idempotencyKey, @Param("operatorId") Long operatorId,
            @Param("remark") String remark);
}
