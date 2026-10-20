package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.api.model.MerchantFinanceSettlementView;

/** 商户财务中心读取租商联盟结算记录。 */
public interface MerchantFinanceSettlementMapper
{
    List<MerchantFinanceSettlementView> select(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("status") Integer status, @Param("beginTime") String beginTime,
            @Param("endTime") String endTime);
}
