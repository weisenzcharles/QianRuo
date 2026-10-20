package com.ruoyi.rentalbusiness.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.mapper.PlatformOperationsAnalyticsMapper;
import com.ruoyi.system.api.model.PlatformOperationsCityStat;
import com.ruoyi.system.api.model.PlatformOperationsMerchantStat;
import com.ruoyi.system.api.model.PlatformOperationsModelStat;
import com.ruoyi.system.api.model.PlatformOperationsTrendView;

/** 平台经营分析查询服务，负责边界校验并将执行面聚合结果交给平台服务编排。 */
@Service
public class PlatformOperationsAnalyticsService
{
    private static final int MAX_DAYS = 366;

    private final PlatformOperationsAnalyticsMapper mapper;

    public PlatformOperationsAnalyticsService(PlatformOperationsAnalyticsMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询周期趋势和商户聚合指标。日期范围按左闭右开处理，避免结束日时分秒遗漏。 */
    public OperationsResult query(List<Long> memberIds, String beginDate, String endDate)
    {
        List<Long> normalizedIds = normalizeMemberIds(memberIds);
        if (normalizedIds.isEmpty())
        {
            return new OperationsResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList());
        }
        LocalDate begin = parseDate(beginDate, "开始日期");
        LocalDate end = parseDate(endDate, "结束日期");
        if (begin.isAfter(end))
        {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        if (begin.plusDays(MAX_DAYS - 1L).isBefore(end))
        {
            throw new ServiceException("经营分析日期范围不能超过366天");
        }
        String beginTime = begin + " 00:00:00";
        String endTime = end.plusDays(1L) + " 00:00:00";
        return new OperationsResult(mapper.selectOrderTrend(normalizedIds, beginTime, endTime),
                mapper.selectMerchantStats(normalizedIds, beginTime, endTime),
                mapper.selectCityStats(normalizedIds, beginTime, endTime),
                mapper.selectModelStats(normalizedIds, beginTime, endTime));
    }

    private List<Long> normalizeMemberIds(List<Long> memberIds)
    {
        Set<Long> normalized = new LinkedHashSet<Long>();
        if (memberIds != null)
        {
            for (Long memberId : memberIds)
            {
                if (memberId != null && memberId.longValue() > 0L)
                {
                    normalized.add(memberId);
                }
            }
        }
        return new ArrayList<Long>(normalized);
    }

    private LocalDate parseDate(String value, String fieldName)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new ServiceException(fieldName + "不能为空");
        }
        try
        {
            return LocalDate.parse(value.trim());
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException(fieldName + "格式不正确，应为 yyyy-MM-dd");
        }
    }

    /** 跨服务返回的两组聚合结果。 */
    public static class OperationsResult
    {
        private final List<PlatformOperationsTrendView> trends;
        private final List<PlatformOperationsMerchantStat> merchantStats;
        private final List<PlatformOperationsCityStat> cityStats;
        private final List<PlatformOperationsModelStat> modelStats;

        public OperationsResult(List<PlatformOperationsTrendView> trends,
                List<PlatformOperationsMerchantStat> merchantStats, List<PlatformOperationsCityStat> cityStats,
                List<PlatformOperationsModelStat> modelStats)
        {
            this.trends = trends == null ? Collections.emptyList() : trends;
            this.merchantStats = merchantStats == null ? Collections.emptyList() : merchantStats;
            this.cityStats = cityStats == null ? Collections.emptyList() : cityStats;
            this.modelStats = modelStats == null ? Collections.emptyList() : modelStats;
        }

        public List<PlatformOperationsTrendView> getTrends() { return trends; }
        public List<PlatformOperationsMerchantStat> getMerchantStats() { return merchantStats; }
        public List<PlatformOperationsCityStat> getCityStats() { return cityStats; }
        public List<PlatformOperationsModelStat> getModelStats() { return modelStats; }
    }
}
