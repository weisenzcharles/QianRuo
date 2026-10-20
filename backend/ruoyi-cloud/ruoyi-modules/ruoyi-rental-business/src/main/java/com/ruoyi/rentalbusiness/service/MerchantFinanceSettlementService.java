package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.api.model.MerchantFinanceSettlementView;
import com.ruoyi.rentalbusiness.mapper.MerchantFinanceSettlementMapper;

/** 租商联盟结算查询服务。 */
@Service
public class MerchantFinanceSettlementService
{
    private final MerchantFinanceSettlementMapper mapper;

    public MerchantFinanceSettlementService(MerchantFinanceSettlementMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 按商户范围查询最近结算记录，并将状态集中转换为中文。 */
    public List<MerchantFinanceSettlementView> list(Long tenantId, Long memberId, Integer status,
            String beginTime, String endTime)
    {
        List<MerchantFinanceSettlementView> rows = mapper.select(tenantId, memberId, status, beginTime, endTime);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        for (MerchantFinanceSettlementView row : rows)
        {
            row.setSettlementStatusName(RentalCode.labelOf(RentalCode.AllianceSettlementStatus.values(),
                    row.getSettlementStatus(), "未知状态"));
        }
        return rows;
    }
}
