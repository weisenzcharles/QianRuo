package com.ruoyi.merchant.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantWithdrawalVO;

/** 商户提现申请及通用余额账户写入。所有查询都带当前商户范围。 */
public interface MerchantFinanceWithdrawalMapper
{
    Long selectLastInsertId();

    Map<String, Object> selectCashAccountForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("accountType") Integer accountType);

    int insertCashAccount(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountType") Integer accountType, @Param("operatorId") Long operatorId);

    int updateCashAccount(@Param("accountId") Long accountId, @Param("availableAmount") BigDecimal availableAmount,
            @Param("frozenAmount") BigDecimal frozenAmount, @Param("version") Long version,
            @Param("operatorId") Long operatorId);

    MerchantWithdrawalVO selectByIdempotency(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("idempotencyKey") String idempotencyKey);

    /** 在提现申请事务内锁定幂等键对应的记录或唯一索引间隙，避免并发请求重复扣款。 */
    MerchantWithdrawalVO selectByIdempotencyForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("idempotencyKey") String idempotencyKey);

    int insertWithdrawal(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountId") Long accountId, @Param("accountType") Integer accountType,
            @Param("withdrawalNo") String withdrawalNo, @Param("idempotencyKey") String idempotencyKey,
            @Param("amount") BigDecimal amount, @Param("fee") BigDecimal fee,
            @Param("actualAmount") BigDecimal actualAmount, @Param("withdrawalChannel") String withdrawalChannel,
            @Param("payeeName") String payeeName, @Param("payeeAccount") String payeeAccount,
            @Param("operatorId") Long operatorId, @Param("remark") String remark);

    MerchantWithdrawalVO selectByNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("withdrawalNo") String withdrawalNo);

    List<MerchantWithdrawalVO> selectList(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("withdrawalStatus") Integer withdrawalStatus);

    int insertFinanceLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerNo") String ledgerNo, @Param("direction") Integer direction,
            @Param("bizType") String bizType, @Param("amount") BigDecimal amount,
            @Param("balanceAfter") BigDecimal balanceAfter, @Param("refId") Long refId,
            @Param("payChannel") String payChannel, @Param("payOrderNo") String payOrderNo,
            @Param("remark") String remark, @Param("operatorId") Long operatorId,
            @Param("accountType") Integer accountType);
}
