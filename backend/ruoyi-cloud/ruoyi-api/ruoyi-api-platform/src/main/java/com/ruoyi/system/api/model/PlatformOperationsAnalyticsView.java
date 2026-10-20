package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台经营分析跨服务聚合结果。 */
public class PlatformOperationsAnalyticsView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<PlatformOperationsTrendView> trends = Collections.emptyList();
    private List<PlatformOperationsMerchantStat> merchantStats = Collections.emptyList();
    private List<PlatformOperationsCityStat> cityStats = Collections.emptyList();
    private List<PlatformOperationsModelStat> modelStats = Collections.emptyList();

    public List<PlatformOperationsTrendView> getTrends() { return trends; }
    public void setTrends(List<PlatformOperationsTrendView> trends) { this.trends = trends; }
    public List<PlatformOperationsMerchantStat> getMerchantStats() { return merchantStats; }
    public void setMerchantStats(List<PlatformOperationsMerchantStat> merchantStats) { this.merchantStats = merchantStats; }
    public List<PlatformOperationsCityStat> getCityStats() { return cityStats; }
    public void setCityStats(List<PlatformOperationsCityStat> cityStats) { this.cityStats = cityStats; }
    public List<PlatformOperationsModelStat> getModelStats() { return modelStats; }
    public void setModelStats(List<PlatformOperationsModelStat> modelStats) { this.modelStats = modelStats; }
}
