package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.LogisticsOrderTrackingRecord;
import com.ruoyi.rentalbusiness.domain.LogisticsProviderCallRecord;
import com.ruoyi.rentalbusiness.domain.LogisticsRouteRecord;
import com.ruoyi.rentalbusiness.domain.MerchantLogisticsListRecord;
import com.ruoyi.rentalbusiness.model.MerchantLogisticsQuery;

/** 商户订单物流路由快照数据访问。所有物流订单查询均带租户、商户和软删除条件。 */
public interface MerchantLogisticsRouteMapper
{
    /** 分页查询当前商户有权访问的物流治理台账。 */
    List<MerchantLogisticsListRecord> selectMerchantLogisticsList(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("query") MerchantLogisticsQuery query);

    /** 按订单编码解析当前商户订单唯一编号，供轨迹操作使用。 */
    Long selectOrderIdByOrderNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderNo") String orderNo);

    /** 查询当前商户指定订单方向的最新物流记录和路由快照元数据。 */
    LogisticsOrderTrackingRecord selectCurrentLogistics(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId,
            @Param("direction") Integer direction);

    /** 读取指定物流记录的已落库路线节点。 */
    List<LogisticsRouteRecord> selectCachedRoutes(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("logisticsId") Long logisticsId);

    /** 查询达到刷新时间且尚未签收的物流记录，供统一定时任务批量处理。 */
    List<LogisticsOrderTrackingRecord> selectRefreshTargets(@Param("limit") Integer limit);

    /** 按物流唯一编号查询平台手工刷新所需的完整物流投影。 */
    LogisticsOrderTrackingRecord selectLogisticsForRefresh(@Param("logisticsId") Long logisticsId);

    /** 软删除指定物流记录的旧路线节点。 */
    int deleteActiveRoutes(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("logisticsId") Long logisticsId, @Param("operatorId") Long operatorId);

    /** 批量插入一次供应商查询得到的标准路线节点。 */
    int insertRouteNodes(@Param("nodes") List<LogisticsRouteRecord> nodes);

    /** 更新物流记录的统一路线状态和最近查询结果。 */
    int updateRouteSnapshot(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("logisticsId") Long logisticsId, @Param("routeStatus") Integer routeStatus,
            @Param("abnormalFlag") Integer abnormalFlag, @Param("routeAvailable") Integer routeAvailable,
            @Param("routeProvider") String routeProvider, @Param("routeMessage") String routeMessage,
            @Param("estimatedArriveTime") LocalDateTime estimatedArriveTime,
            @Param("operatorId") Long operatorId);

    /** 清理物流信息变更后的路线缓存，确保下一次查询重新读取供应商数据。 */
    int resetRouteSnapshot(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("logisticsId") Long logisticsId, @Param("operatorId") Long operatorId);

    /** 追加物流供应商调用审计，不保存供应商原始响应报文。 */
    int insertProviderCallLog(@Param("record") LogisticsProviderCallRecord record);
}
