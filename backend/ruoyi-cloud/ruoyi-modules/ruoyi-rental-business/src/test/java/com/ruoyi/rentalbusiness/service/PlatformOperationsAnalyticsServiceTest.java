package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.mapper.PlatformOperationsAnalyticsMapper;
import com.ruoyi.system.api.model.PlatformOperationsMerchantStat;

/** 租赁业务经营分析内部查询的范围去重和日期边界测试。 */
public class PlatformOperationsAnalyticsServiceTest
{
    @Test
    public void query_deduplicatesMemberIdsBeforeDatabaseQuery()
    {
        AtomicReference<List<Long>> requestedIds = new AtomicReference<List<Long>>();
        PlatformOperationsAnalyticsMapper mapper = mapper((method, args) -> {
            if ("selectMerchantStats".equals(method))
                requestedIds.set((List<Long>) args[0]);
            return List.of();
        });

        PlatformOperationsAnalyticsService.OperationsResult result = new PlatformOperationsAnalyticsService(mapper)
                .query(List.of(27L, 27L, 28L), "2026-09-01", "2026-09-07");

        Assert.assertEquals(List.of(27L, 28L), requestedIds.get());
        Assert.assertTrue(result.getTrends().isEmpty());
        Assert.assertTrue(result.getMerchantStats().isEmpty());
    }

    @Test
    public void query_rejectsDateRangeBeyondOneYear()
    {
        PlatformOperationsAnalyticsMapper mapper = mapper((method, args) -> {
            throw new AssertionError("invalid date range must not query database");
        });
        try
        {
            new PlatformOperationsAnalyticsService(mapper).query(List.of(27L), "2025-01-01", "2026-01-02");
            Assert.fail("date range beyond one year must be rejected");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("经营分析日期范围不能超过366天", expected.getMessage());
        }
    }

    private PlatformOperationsAnalyticsMapper mapper(MapperCall call)
    {
        return (PlatformOperationsAnalyticsMapper) Proxy.newProxyInstance(
                PlatformOperationsAnalyticsMapper.class.getClassLoader(),
                new Class<?>[] { PlatformOperationsAnalyticsMapper.class },
                (proxy, method, args) -> call.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
