package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.rentalbusiness.domain.LogisticsOrderTrackingRecord;
import com.ruoyi.rentalbusiness.domain.LogisticsRouteRecord;
import com.ruoyi.rentalbusiness.mapper.MerchantLogisticsRouteMapper;

/** 物流路线快照写入服务，集中维护路线节点和物流状态的一致性。 */
@Service
public class LogisticsRouteSnapshotService
{
    private final MerchantLogisticsRouteMapper routeMapper;

    public LogisticsRouteSnapshotService(MerchantLogisticsRouteMapper routeMapper)
    {
        this.routeMapper = routeMapper;
    }

    /**
     * 原子替换一次供应商查询结果。
     *
     * <p>先软删除旧节点，再插入新节点并更新物流主记录的快照元数据；任一步失败都会回滚，
     * 避免出现主状态已更新但路线节点仍是旧数据的半成品。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSnapshot(LogisticsOrderTrackingRecord target, Integer routeStatus, Integer abnormalFlag,
            Integer routeAvailable, String routeProvider, String routeMessage,
            List<LogisticsRouteRecord> nodes, LocalDateTime estimatedArriveTime, Long operatorId)
    {
        if (target == null || target.logisticsId == null)
        {
            throw new IllegalArgumentException("物流记录不能为空");
        }
        routeMapper.deleteActiveRoutes(target.tenantId, target.memberId, target.logisticsId, operatorId);
        List<LogisticsRouteRecord> actualNodes = nodes == null ? Collections.emptyList() : nodes;
        if (!actualNodes.isEmpty())
        {
            routeMapper.insertRouteNodes(actualNodes);
        }
        int updated = routeMapper.updateRouteSnapshot(target.tenantId, target.memberId, target.logisticsId,
                routeStatus, abnormalFlag, routeAvailable, routeProvider, routeMessage, estimatedArriveTime,
                operatorId);
        if (updated == 0)
        {
            throw new IllegalStateException("物流记录不存在或无权更新");
        }
    }

    /** 清理物流公司或快递单号修改后的旧路线。 */
    @Transactional(rollbackFor = Exception.class)
    public void resetSnapshot(Long tenantId, Long memberId, Long logisticsId, Long operatorId)
    {
        if (logisticsId == null)
        {
            return;
        }
        routeMapper.deleteActiveRoutes(tenantId, memberId, logisticsId, operatorId);
        routeMapper.resetRouteSnapshot(tenantId, memberId, logisticsId, operatorId);
    }
}
