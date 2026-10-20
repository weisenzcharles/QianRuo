package com.ruoyi.system.mapper.rental;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformFinanceAccountVO;
import com.ruoyi.system.model.rental.PlatformFinanceLedgerVO;
import com.ruoyi.system.model.rental.PlatformFinanceQuery;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalVO;
import com.ruoyi.system.model.rental.PlatformPaymentOrderVO;

/** 平台财务只读查询数据访问组件。 */
public interface PlatformFinanceMapper
{
    Map<String, Object> selectSummary(@Param("monthStart") String monthStart);

    /** 查询待审核提现数量，供平台运营风险概览使用。 */
    int countPendingWithdrawals();

    List<PlatformFinanceAccountVO> selectAccounts(@Param("query") PlatformFinanceQuery query);

    List<PlatformFinanceLedgerVO> selectLedger(@Param("query") PlatformFinanceQuery query);

    PlatformFinanceLedgerVO selectLedgerByNo(@Param("ledgerNo") String ledgerNo);

    List<PlatformPaymentOrderVO> selectPaymentOrders(@Param("query") PlatformFinanceQuery query);

    PlatformPaymentOrderVO selectPaymentOrderByNo(@Param("paymentNo") String paymentNo);

    List<PlatformFinanceWithdrawalVO> selectWithdrawals(@Param("query") PlatformFinanceQuery query);

    PlatformFinanceWithdrawalVO selectWithdrawalByNo(@Param("withdrawalNo") String withdrawalNo);

    Map<String, Object> selectWithdrawalForUpdate(@Param("withdrawalNo") String withdrawalNo);

    Map<String, Object> selectAccountForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountType") Integer accountType);

    int updateAccount(@Param("accountId") Long accountId, @Param("availableAmount") java.math.BigDecimal availableAmount,
            @Param("frozenAmount") java.math.BigDecimal frozenAmount, @Param("version") Long version,
            @Param("operatorId") Long operatorId);

    int updateWithdrawal(@Param("withdrawalId") Long withdrawalId, @Param("withdrawalStatus") Integer withdrawalStatus,
            @Param("reviewRemark") String reviewRemark, @Param("reviewerId") Long reviewerId);

    int insertWithdrawalLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerNo") String ledgerNo, @Param("direction") Integer direction,
            @Param("amount") java.math.BigDecimal amount, @Param("balanceAfter") java.math.BigDecimal balanceAfter,
            @Param("refId") Long refId, @Param("payChannel") String payChannel,
            @Param("payOrderNo") String payOrderNo, @Param("remark") String remark, @Param("operatorId") Long operatorId,
            @Param("accountType") Integer accountType);
}
