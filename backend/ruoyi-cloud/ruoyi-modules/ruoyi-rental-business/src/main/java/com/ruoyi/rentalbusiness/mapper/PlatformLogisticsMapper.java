package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformLogisticsQuery;
import com.ruoyi.system.api.model.PlatformLogisticsCallView;
import com.ruoyi.system.api.model.PlatformLogisticsRouteView;
import com.ruoyi.system.api.model.PlatformLogisticsView;

/** 平台物流治理数据访问。查询只允许从平台内部接口进入。 */
public interface PlatformLogisticsMapper
{
    /** 统计平台可见物流记录。 */
    long countLogistics(@Param("query") PlatformLogisticsQuery query);

    /** 分页查询平台可见物流记录。 */
    List<PlatformLogisticsView> selectLogistics(@Param("query") PlatformLogisticsQuery query,
            @Param("offset") int offset, @Param("pageSize") int pageSize);

    /** 查询单条物流记录，详情页使用。 */
    PlatformLogisticsView selectLogisticsById(@Param("logisticsId") Long logisticsId);

    /** 查询指定订单的全部物流单，订单详情只读页面使用。 */
    List<PlatformLogisticsView> selectLogisticsByOrder(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    /** 查询物流记录已落库的路线节点。 */
    List<PlatformLogisticsRouteView> selectRoutes(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("logisticsId") Long logisticsId);

    /** 查询物流供应商调用审计，单号由 SQL 只返回脱敏值。 */
    List<PlatformLogisticsCallView> selectProviderCalls(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("logisticsId") Long logisticsId);
}
