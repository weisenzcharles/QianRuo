package com.ruoyi.rentalbusiness.integration.express;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 快递轨迹客户端门面，供订单查询和定时同步任务复用。 */
@Service
public class ExpressTrackingService
{
    private final Map<ExpressProvider, ExpressTrackingClient> clients;

    public ExpressTrackingService(List<ExpressTrackingClient> trackingClients)
    {
        this.clients = new EnumMap<>(ExpressProvider.class);
        trackingClients.forEach(client -> clients.put(client.provider(), client));
    }

    /** 根据供应商查询快递轨迹，客户端不存在时返回统一不可用结果。 */
    public ExpressTrackResult query(ExpressTrackQuery query)
    {
        ExpressTrackQuery normalized = query.normalized();
        ExpressTrackingClient client = clients.get(normalized.provider());
        return client == null
                ? ExpressTrackResult.unavailable(normalized.provider(), normalized.trackingNo(), "暂不支持该快递轨迹服务")
                : client.query(normalized);
    }
}
