package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.api.model.PlatformLogisticsQuery;
import com.ruoyi.system.api.model.PlatformLogisticsCallView;
import com.ruoyi.system.api.model.PlatformLogisticsRouteView;
import com.ruoyi.system.api.model.PlatformLogisticsView;
import com.ruoyi.rentalbusiness.mapper.PlatformLogisticsMapper;

/** 平台物流治理的枚举中文转换和详情路线查询测试。 */
public class PlatformLogisticsServiceTest
{
    @Test
    public void listDecoratesChineseLogisticsLabels()
    {
        StubMapper mapper = new StubMapper();
        PlatformLogisticsView row = mapper.row();
        row.setDirection(Integer.valueOf(1));
        mapper.rows = Collections.singletonList(row);

        PlatformLogisticsService service = new PlatformLogisticsService(mapper);
        PlatformLogisticsView result = service.list(new PlatformLogisticsQuery()).getRows().get(0);

        Assert.assertEquals("发货", result.getDirectionName());
        Assert.assertEquals("人工录入", result.getSourceTypeName());
        Assert.assertEquals("运输中", result.getRouteStatusName());
    }

    @Test
    public void detailDecoratesCachedRouteLabels()
    {
        StubMapper mapper = new StubMapper();
        PlatformLogisticsRouteView route = new PlatformLogisticsRouteView();
        route.setRouteStatus(Integer.valueOf(2));
        route.setRouteDesc("已签收");
        mapper.routes = Collections.singletonList(route);
        PlatformLogisticsCallView call = new PlatformLogisticsCallView();
        call.setProvider("sf");
        call.setAvailable(Integer.valueOf(0));
        call.setRouteStatus(Integer.valueOf(3));
        call.setErrorType("provider_unavailable");
        mapper.calls = Collections.singletonList(call);

        PlatformLogisticsDetailViewHolder holder = new PlatformLogisticsDetailViewHolder(
                new PlatformLogisticsService(mapper).detail(Long.valueOf(11L)));

        Assert.assertEquals("已签收", holder.detail.getRoutes().get(0).getRouteStatusName());
        Assert.assertEquals("归还", holder.detail.getLogistics().getDirectionName());
        Assert.assertEquals("顺丰", holder.detail.getCalls().get(0).getProviderName());
        Assert.assertEquals("查询失败", holder.detail.getCalls().get(0).getAvailableName());
        Assert.assertEquals("异常", holder.detail.getCalls().get(0).getRouteStatusName());
        Assert.assertEquals("供应商未返回有效轨迹", holder.detail.getCalls().get(0).getErrorTypeName());
    }

    private static class PlatformLogisticsDetailViewHolder
    {
        private final com.ruoyi.system.api.model.PlatformLogisticsDetailView detail;

        private PlatformLogisticsDetailViewHolder(com.ruoyi.system.api.model.PlatformLogisticsDetailView value)
        {
            detail = value;
        }
    }

    private static class StubMapper implements PlatformLogisticsMapper
    {
        private List<PlatformLogisticsView> rows = Collections.emptyList();
        private List<PlatformLogisticsRouteView> routes = Collections.emptyList();
        private List<PlatformLogisticsCallView> calls = Collections.emptyList();

        @Override
        public long countLogistics(PlatformLogisticsQuery query) { return rows.size(); }

        @Override
        public List<PlatformLogisticsView> selectLogistics(PlatformLogisticsQuery query, int offset, int pageSize)
        {
            return rows;
        }

        @Override
        public PlatformLogisticsView selectLogisticsById(Long logisticsId) { return row(); }

        @Override
        public List<PlatformLogisticsView> selectLogisticsByOrder(Long tenantId, Long memberId, Long orderId)
        {
            return rows;
        }

        @Override
        public List<PlatformLogisticsRouteView> selectRoutes(Long tenantId, Long memberId, Long logisticsId)
        {
            return routes;
        }

        @Override
        public List<PlatformLogisticsCallView> selectProviderCalls(Long tenantId, Long memberId, Long logisticsId)
        {
            return calls;
        }

        private PlatformLogisticsView row()
        {
            PlatformLogisticsView row = new PlatformLogisticsView();
            row.setLogisticsId(Long.valueOf(11L));
            row.setTenantId(Long.valueOf(1L));
            row.setMemberId(Long.valueOf(2L));
            row.setDirection(Integer.valueOf(2));
            row.setSourceType(Integer.valueOf(2));
            row.setRouteStatus(Integer.valueOf(1));
            return row;
        }
    }
}
