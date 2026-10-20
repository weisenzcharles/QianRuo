package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台物流详情和已缓存路线节点。 */
public class PlatformLogisticsDetailView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PlatformLogisticsView logistics;
    private List<PlatformLogisticsRouteView> routes = Collections.emptyList();
    private List<PlatformLogisticsCallView> calls = Collections.emptyList();

    public PlatformLogisticsView getLogistics() { return logistics; }
    public void setLogistics(PlatformLogisticsView value) { logistics = value; }
    public List<PlatformLogisticsRouteView> getRoutes() { return routes; }
    public void setRoutes(List<PlatformLogisticsRouteView> value) { routes = value; }
    public List<PlatformLogisticsCallView> getCalls() { return calls; }
    public void setCalls(List<PlatformLogisticsCallView> value) { calls = value; }
}
