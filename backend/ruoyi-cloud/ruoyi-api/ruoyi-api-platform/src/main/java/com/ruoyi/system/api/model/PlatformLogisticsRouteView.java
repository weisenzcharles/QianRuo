package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台物流路线节点视图。 */
public class PlatformLogisticsRouteView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer routeStatus;
    private String routeStatusName;
    private String routeDesc;
    private String routeTime;
    private String location;

    public Integer getRouteStatus() { return routeStatus; }
    public void setRouteStatus(Integer value) { routeStatus = value; }
    public String getRouteStatusName() { return routeStatusName; }
    public void setRouteStatusName(String value) { routeStatusName = value; }
    public String getRouteDesc() { return routeDesc; }
    public void setRouteDesc(String value) { routeDesc = value; }
    public String getRouteTime() { return routeTime; }
    public void setRouteTime(String value) { routeTime = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }
}
