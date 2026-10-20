package com.ruoyi.system.model.rental;

/** 平台订单详情中的已缓存物流节点。 */
public class PlatformOrderRouteVO
{
    private String routeStatusName;
    private String routeDesc;
    private String routeTime;
    private String location;

    public String getRouteStatusName() { return routeStatusName; }
    public void setRouteStatusName(String value) { routeStatusName = value; }
    public String getRouteDesc() { return routeDesc; }
    public void setRouteDesc(String value) { routeDesc = value; }
    public String getRouteTime() { return routeTime; }
    public void setRouteTime(String value) { routeTime = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }
}
