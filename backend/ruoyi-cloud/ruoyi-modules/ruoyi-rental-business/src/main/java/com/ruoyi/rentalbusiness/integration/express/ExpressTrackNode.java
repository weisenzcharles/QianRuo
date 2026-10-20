package com.ruoyi.rentalbusiness.integration.express;

import java.time.OffsetDateTime;

/** 标准化后的单条快递轨迹节点。 */
public record ExpressTrackNode(OffsetDateTime time, String status, String description, String location)
{
}
