package com.ruoyi.merchant.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantFinanceLedgerQuery;
import com.ruoyi.merchant.model.MerchantFinanceLedgerVO;

/** 商户财务流水只读查询。所有方法都必须带租户和商户范围。 */
public interface MerchantFinanceMapper
{
    Map<String, Object> selectOverview(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("monthStart") String monthStart);

    List<Map<String, Object>> selectAccounts(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    List<MerchantFinanceLedgerVO> selectLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("query") MerchantFinanceLedgerQuery query);

    MerchantFinanceLedgerVO selectLedgerByNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerNo") String ledgerNo);

    List<MerchantFinanceLedgerVO> selectRecentLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
}
