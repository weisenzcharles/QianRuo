package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import com.ruoyi.rentalbusiness.mapper.PlatformExecutionWorkbenchMapper;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchTaskView;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchView;
import org.junit.Assert;
import org.junit.Test;

/** 平台执行面待办汇总和中文状态测试。 */
public class PlatformExecutionWorkbenchServiceTest
{
    @Test
    public void overview_aggregatesFourExecutionTaskTypesAndDecoratesStatuses()
    {
        StubMapper mapper = new StubMapper();
        mapper.fulfillmentTasks = Collections.singletonList(task(6, 60));
        mapper.afterSalesTasks = Collections.singletonList(task(7, 0));
        mapper.logisticsTasks = Collections.singletonList(task(8, 3));
        mapper.inventoryTasks = Collections.singletonList(task(9, 0));

        PlatformExecutionWorkbenchView result = new PlatformExecutionWorkbenchService(mapper).overview();

        Assert.assertEquals(Boolean.TRUE, result.getAvailable());
        Assert.assertEquals(Integer.valueOf(1), result.getFulfillmentTaskCount());
        Assert.assertEquals(Integer.valueOf(1), result.getAfterSalesTaskCount());
        Assert.assertEquals(Integer.valueOf(1), result.getLogisticsTaskCount());
        Assert.assertEquals(Integer.valueOf(1), result.getInventoryAlertTaskCount());
        Assert.assertEquals(4, result.getTasks().size());
        Assert.assertEquals("归还逾期", result.getTasks().get(0).getTaskStatusName());
        Assert.assertEquals("处理中", result.getTasks().get(1).getTaskStatusName());
        Assert.assertEquals("异常", result.getTasks().get(2).getTaskStatusName());
        Assert.assertEquals("待处理", result.getTasks().get(3).getTaskStatusName());
    }

    private PlatformExecutionWorkbenchTaskView task(int type, int status)
    {
        PlatformExecutionWorkbenchTaskView task = new PlatformExecutionWorkbenchTaskView();
        task.setTaskType(Integer.valueOf(type));
        task.setTaskStatus(Integer.valueOf(status));
        return task;
    }

    private static class StubMapper implements PlatformExecutionWorkbenchMapper
    {
        private List<PlatformExecutionWorkbenchTaskView> fulfillmentTasks = Collections.emptyList();
        private List<PlatformExecutionWorkbenchTaskView> afterSalesTasks = Collections.emptyList();
        private List<PlatformExecutionWorkbenchTaskView> logisticsTasks = Collections.emptyList();
        private List<PlatformExecutionWorkbenchTaskView> inventoryTasks = Collections.emptyList();

        @Override public int countOpenFulfillmentTasks() { return fulfillmentTasks.size(); }
        @Override public int countOpenAfterSalesTasks() { return afterSalesTasks.size(); }
        @Override public int countOpenLogisticsTasks() { return logisticsTasks.size(); }
        @Override public int countOpenInventoryAlertTasks() { return inventoryTasks.size(); }
        @Override public List<PlatformExecutionWorkbenchTaskView> selectFulfillmentTasks(Integer limit) { return fulfillmentTasks; }
        @Override public List<PlatformExecutionWorkbenchTaskView> selectAfterSalesTasks(Integer limit) { return afterSalesTasks; }
        @Override public List<PlatformExecutionWorkbenchTaskView> selectLogisticsTasks(Integer limit) { return logisticsTasks; }
        @Override public List<PlatformExecutionWorkbenchTaskView> selectInventoryAlertTasks(Integer limit) { return inventoryTasks; }
    }
}
