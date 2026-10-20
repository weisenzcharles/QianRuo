package com.ruoyi.system.rental;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.mapper.rental.PlatformAlertEventMapper;
import com.ruoyi.system.model.rental.PlatformAlertEventQuery;
import com.ruoyi.system.model.rental.PlatformAlertEventVO;
import com.ruoyi.system.service.rental.PlatformAlertEventService;
import com.ruoyi.system.api.model.PlatformExternalInterfaceAlertCommand;

/** 平台运维告警同步、中文枚举和处理状态测试。 */
public class PlatformAlertEventServiceTest
{
    @Test
    public void list_syncsFailedScheduledTaskAndDecoratesLabels()
    {
        StubMapper mapper = new StubMapper();
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("alertKey", "JOB_FAILURE:库存刷新:inventoryAlertRefreshJob.refresh()");
        source.put("title", "调度任务执行失败：库存刷新");
        source.put("detail", "任务异常");
        source.put("sourceKey", "inventoryAlertRefreshJob.refresh()");
        source.put("occurrenceCount", Long.valueOf(2L));
        mapper.sources = Collections.singletonList(source);
        PlatformAlertEventVO event = event(7L, 1, 3, 0);
        mapper.rows = Collections.singletonList(event);

        List<PlatformAlertEventVO> result = new PlatformAlertEventService(mapper).list(new PlatformAlertEventQuery());

        Assert.assertEquals(1, mapper.upsertCount);
        Assert.assertEquals("调度任务", result.get(0).getAlertTypeName());
        Assert.assertEquals("高风险", result.get(0).getAlertLevelName());
        Assert.assertEquals("待处理", result.get(0).getEventStatusName());
        Assert.assertEquals(1, mapper.resolveCount);
    }

    @Test
    public void resolve_addsOperatorRemark()
    {
        StubMapper mapper = new StubMapper();
        mapper.event = event(8L, 1, 3, 1);

        new PlatformAlertEventService(mapper).resolve(Long.valueOf(8L), "已重启调度服务");

        Assert.assertEquals("已重启调度服务", mapper.remark);
        Assert.assertEquals(1, mapper.resolveCallCount);
    }

    @Test
    public void synchronizeScheduledTaskAlerts_returnsDiscoveredSourceCount()
    {
        StubMapper mapper = new StubMapper();
        mapper.sources = Collections.singletonList(new HashMap<String, Object>());

        int count = new PlatformAlertEventService(mapper).synchronizeScheduledTaskAlerts();

        Assert.assertEquals(1, count);
        Assert.assertEquals(1, mapper.resolveCount);
    }

    @Test
    public void externalInterfaceAlert_upsertsAndResolvesByBusinessKey()
    {
        StubMapper mapper = new StubMapper();
        PlatformExternalInterfaceAlertCommand command = new PlatformExternalInterfaceAlertCommand();
        command.setAlertEventKey("LOGISTICS_QUERY_FAILURE:11");
        command.setAlertLevel(Integer.valueOf(3));
        command.setTitle("物流查询失败");
        command.setDetail("顺丰与快递100均未返回有效轨迹");
        command.setSourceKey("logistics:11");

        PlatformAlertEventService service = new PlatformAlertEventService(mapper);
        service.upsertExternalInterfaceAlert(command);
        service.resolveExternalInterfaceAlert(command.getAlertEventKey());

        Assert.assertEquals("LOGISTICS_QUERY_FAILURE:11", mapper.externalAlertKey);
        Assert.assertEquals(Integer.valueOf(3), mapper.externalAlertLevel);
        Assert.assertEquals("LOGISTICS_QUERY_FAILURE:11", mapper.resolvedExternalAlertKey);
    }

    private PlatformAlertEventVO event(long id, int type, int level, int status)
    {
        PlatformAlertEventVO value = new PlatformAlertEventVO();
        value.setAlertEventId(Long.valueOf(id));
        value.setAlertType(Integer.valueOf(type));
        value.setAlertLevel(Integer.valueOf(level));
        value.setEventStatus(Integer.valueOf(status));
        value.setTitle("测试告警");
        return value;
    }

    private static class StubMapper implements PlatformAlertEventMapper
    {
        private List<Map<String, Object>> sources = Collections.emptyList();
        private List<PlatformAlertEventVO> rows = Collections.emptyList();
        private PlatformAlertEventVO event;
        private int upsertCount;
        private int resolveCount;
        private int resolveCallCount;
        private String remark;
        private String externalAlertKey;
        private Integer externalAlertLevel;
        private String resolvedExternalAlertKey;

        @Override public List<PlatformAlertEventVO> selectList(PlatformAlertEventQuery query) { return rows; }
        @Override public List<Map<String, Object>> selectRecentFailedJobs() { return sources; }
        @Override public int upsertScheduledTaskAlert(String alertEventKey, String title, String detail,
                String sourceKey, Long occurrenceCount, Long operator) { upsertCount++; return 1; }
        @Override public int upsertStorageAlert(String alertEventKey, Integer alertType, Integer alertLevel,
                String title, String detail, String sourceKey, Long operator) { return 1; }
        @Override public int resolveStorageAlert(String alertEventKey, Long operator, String remark) { return 1; }
        @Override public int upsertExternalInterfaceAlert(String alertEventKey, Integer alertLevel, String title,
                String detail, String sourceKey, Long operator)
        {
            externalAlertKey = alertEventKey;
            externalAlertLevel = alertLevel;
            return 1;
        }
        @Override public int resolveExternalInterfaceAlert(String alertEventKey, Long operator, String remark)
        {
            resolvedExternalAlertKey = alertEventKey;
            return 1;
        }
        @Override public int resolveMissingScheduledTaskAlerts(List<String> alertEventKeys, Long operator) { resolveCount++; return 1; }
        @Override public int countOpenEvents() { return 0; }
        @Override public PlatformAlertEventVO selectById(Long alertEventId) { return event; }
        @Override public int acknowledge(Long alertEventId, Long operator) { return 1; }
        @Override public int resolve(Long alertEventId, String value, Long operator)
        {
            resolveCallCount++;
            remark = value;
            return 1;
        }
    }
}
