package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.rentalbusiness.mapper.PlatformAllianceOrderMapper;
import com.ruoyi.system.api.model.PlatformAllianceOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderDetailView;
import com.ruoyi.system.api.model.PlatformAllianceOrderView;
import com.ruoyi.system.api.model.PlatformAllianceSettlementSummaryView;

/** 平台联盟订单只读查询服务。 */
@Service
public class PlatformAllianceOrderQueryService
{
    private final PlatformAllianceOrderMapper mapper;

    public PlatformAllianceOrderQueryService(PlatformAllianceOrderMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 按创建时间倒序分页查询联盟协助订单。 */
    public PlatformAllianceOrderPageView list(com.ruoyi.system.api.model.PlatformAllianceOrderQuery query)
    {
        int page = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int size = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100);
        long total = mapper.count(query);
        List<com.ruoyi.system.api.model.PlatformAllianceOrderView> rows = total == 0
                ? Collections.emptyList() : mapper.select(query, (page - 1) * size, size);
        PlatformAllianceOrderPageView result = new PlatformAllianceOrderPageView();
        result.setRows(rows);
        result.setTotal(total);
        return result;
    }

    /** 查询联盟订单、不可变事件和结算记账详情。 */
    public PlatformAllianceOrderDetailView detail(Long allianceOrderId)
    {
        PlatformAllianceOrderView order = mapper.selectById(allianceOrderId);
        if (order == null)
        {
            return null;
        }
        PlatformAllianceOrderDetailView result = new PlatformAllianceOrderDetailView();
        result.setOrder(order);
        result.setEvents(mapper.selectEvents(allianceOrderId));
        result.setSettlements(mapper.selectSettlements(allianceOrderId));
        return result;
    }

    /** 查询全部有效联盟订单的结算汇总，供平台经营分析使用。 */
    public PlatformAllianceSettlementSummaryView settlementSummary()
    {
        PlatformAllianceSettlementSummaryView result = mapper.selectSettlementSummary();
        return result == null ? new PlatformAllianceSettlementSummaryView() : result;
    }
}
