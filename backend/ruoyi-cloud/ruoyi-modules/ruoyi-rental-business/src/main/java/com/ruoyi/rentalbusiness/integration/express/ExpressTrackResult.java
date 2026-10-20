package com.ruoyi.rentalbusiness.integration.express;

import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

/** 统一的快递轨迹查询结果，屏蔽供应商返回报文差异。 */
public record ExpressTrackResult(boolean available, ExpressProvider provider, String trackingNo,
        String status, List<ExpressTrackNode> nodes, String message, LocalDateTime estimatedArriveTime)
{
    /** 兼容暂不提供预计到达时间的供应商结果。 */
    public ExpressTrackResult(boolean available, ExpressProvider provider, String trackingNo,
            String status, List<ExpressTrackNode> nodes, String message)
    {
        this(available, provider, trackingNo, status, nodes, message, null);
    }

    public ExpressTrackResult
    {
        nodes = nodes == null ? Collections.emptyList() : List.copyOf(nodes);
    }

    public static ExpressTrackResult unavailable(ExpressProvider provider, String trackingNo, String message)
    {
        return new ExpressTrackResult(false, provider, trackingNo, null, Collections.emptyList(), message);
    }
}
