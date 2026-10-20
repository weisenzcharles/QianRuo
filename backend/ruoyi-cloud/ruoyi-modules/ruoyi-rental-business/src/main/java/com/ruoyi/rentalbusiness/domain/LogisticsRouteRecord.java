package com.ruoyi.rentalbusiness.domain;

import java.time.LocalDateTime;

/** 已标准化并落库的单条物流路线节点。 */
public class LogisticsRouteRecord
{
    public Long routeId;
    public Long logisticsId;
    public String trackingNo;
    public Integer routeStatus;
    public String routeDesc;
    public LocalDateTime routeTime;
    public String province;
    public String city;
    public String rawJson;
    public LocalDateTime createTime;
}
