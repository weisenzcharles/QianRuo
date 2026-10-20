package com.ruoyi.system.model.rental;

import java.util.Collections;
import java.util.List;

/** 平台经营分析与运营风控页面数据。 */
public class PlatformOperationsOverviewVO
{
    private String beginDate;
    private String endDate;
    private PlatformOperationsSummaryVO summary = new PlatformOperationsSummaryVO();
    private List<PlatformOperationsTrendVO> trends = Collections.emptyList();
    private List<PlatformOperationsMerchantVO> merchants = Collections.emptyList();
    private List<PlatformOperationsCityVO> cityStats = Collections.emptyList();
    private List<PlatformOperationsModelVO> modelStats = Collections.emptyList();
    private List<PlatformOperationsRiskVO> risks = Collections.emptyList();

    public String getBeginDate() { return beginDate; }
    public void setBeginDate(String beginDate) { this.beginDate = beginDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public PlatformOperationsSummaryVO getSummary() { return summary; }
    public void setSummary(PlatformOperationsSummaryVO summary) { this.summary = summary; }
    public List<PlatformOperationsTrendVO> getTrends() { return trends; }
    public void setTrends(List<PlatformOperationsTrendVO> trends) { this.trends = trends; }
    public List<PlatformOperationsMerchantVO> getMerchants() { return merchants; }
    public void setMerchants(List<PlatformOperationsMerchantVO> merchants) { this.merchants = merchants; }
    public List<PlatformOperationsCityVO> getCityStats() { return cityStats; }
    public void setCityStats(List<PlatformOperationsCityVO> cityStats) { this.cityStats = cityStats; }
    public List<PlatformOperationsModelVO> getModelStats() { return modelStats; }
    public void setModelStats(List<PlatformOperationsModelVO> modelStats) { this.modelStats = modelStats; }
    public List<PlatformOperationsRiskVO> getRisks() { return risks; }
    public void setRisks(List<PlatformOperationsRiskVO> risks) { this.risks = risks; }
}
