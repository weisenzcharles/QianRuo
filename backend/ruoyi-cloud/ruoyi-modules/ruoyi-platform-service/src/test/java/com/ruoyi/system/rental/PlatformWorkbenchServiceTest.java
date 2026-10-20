package com.ruoyi.system.rental;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import com.ruoyi.system.mapper.rental.PlatformWorkbenchMapper;
import com.ruoyi.system.model.rental.PlatformWorkbenchOverviewVO;
import com.ruoyi.system.model.rental.PlatformWorkbenchTaskVO;
import com.ruoyi.system.service.rental.PlatformWorkbenchService;
import org.junit.Assert;
import org.junit.Test;

/** 平台工作台汇总、中文状态和优先级排序测试。 */
public class PlatformWorkbenchServiceTest
{
    @Test
    public void overview_aggregatesAndSortsTasks()
    {
        StubWorkbenchMapper mapper = new StubWorkbenchMapper();
        PlatformWorkbenchTaskVO ticket = task(2, 1, 1, "2026-09-26 10:00:00");
        PlatformWorkbenchTaskVO risk = task(1, 3, 0, "2026-09-26 11:00:00");
        risk.setBusinessNo("9");
        PlatformWorkbenchTaskVO invoice = task(4, 2, 2, "2026-09-26 09:00:00");
        PlatformWorkbenchTaskVO marketReport = task(5, 1, 0, "2026-09-26 08:00:00");
        mapper.riskTasks = Collections.singletonList(risk);
        mapper.ticketTasks = Collections.singletonList(ticket);
        mapper.invoiceTasks = Collections.singletonList(invoice);
        mapper.marketReportTasks = Collections.singletonList(marketReport);

        PlatformWorkbenchOverviewVO result = new PlatformWorkbenchService(mapper).overview();

        Assert.assertEquals(Integer.valueOf(5), result.getTotalTaskCount());
        Assert.assertEquals(Integer.valueOf(1), result.getRiskTaskCount());
        Assert.assertEquals("运营风险", result.getTasks().get(0).getTaskTypeName());
        Assert.assertEquals("紧急", result.getTasks().get(0).getPriorityName());
        Assert.assertEquals("/operations/analytics?riskEventId=9", result.getTasks().get(0).getTargetPath());
        Assert.assertEquals("发票审核", result.getTasks().get(1).getTaskTypeName());
        Assert.assertEquals("待开具", result.getTasks().get(1).getTaskStatusName());
        Assert.assertEquals("市场举报", result.getTasks().get(2).getTaskTypeName());
        Assert.assertEquals("待处理", result.getTasks().get(2).getTaskStatusName());
        Assert.assertEquals("处理中", result.getTasks().get(3).getTaskStatusName());
        Assert.assertEquals(new BigDecimal("18.00"), result.getPendingWithdrawalAmount());
    }

    @Test
    public void overview_buildsPlatformRouteForTask()
    {
        StubWorkbenchMapper mapper = new StubWorkbenchMapper();
        PlatformWorkbenchTaskVO ticket = task(2, 1, 1, "2026-09-26 10:00:00");
        ticket.setBusinessNo("TK 001");
        mapper.ticketTasks = Collections.singletonList(ticket);

        PlatformWorkbenchOverviewVO result = new PlatformWorkbenchService(mapper).overview();

        Assert.assertEquals("/governance/tickets?ticketNo=TK+001",
                result.getTasks().get(0).getTargetPath());
    }

    private PlatformWorkbenchTaskVO task(int type, int priority, int status, String createTime)
    {
        PlatformWorkbenchTaskVO task = new PlatformWorkbenchTaskVO();
        task.setTaskId(Long.valueOf(type));
        task.setTaskType(Integer.valueOf(type));
        task.setPriority(Integer.valueOf(priority));
        task.setTaskStatus(Integer.valueOf(status));
        task.setCreateTime(createTime);
        return task;
    }

    private static class StubWorkbenchMapper implements PlatformWorkbenchMapper
    {
        private List<PlatformWorkbenchTaskVO> riskTasks = Collections.emptyList();
        private List<PlatformWorkbenchTaskVO> ticketTasks = Collections.emptyList();
        private List<PlatformWorkbenchTaskVO> invoiceTasks = Collections.emptyList();
        private List<PlatformWorkbenchTaskVO> marketReportTasks = Collections.emptyList();

        @Override public int countOpenRiskTasks() { return 1; }
        @Override public int countOpenTicketTasks() { return 1; }
        @Override public int countPendingWithdrawalTasks() { return 1; }
        @Override public int countOpenInvoiceTasks() { return invoiceTasks.size(); }
        @Override public int countOpenMarketReportTasks() { return marketReportTasks.size(); }
        @Override public BigDecimal sumPendingWithdrawalAmount() { return new BigDecimal("18.00"); }
        @Override public List<PlatformWorkbenchTaskVO> selectRiskTasks(Integer limit) { return riskTasks; }
        @Override public List<PlatformWorkbenchTaskVO> selectTicketTasks(Integer limit) { return ticketTasks; }
        @Override public List<PlatformWorkbenchTaskVO> selectWithdrawalTasks(Integer limit) { return Collections.emptyList(); }
        @Override public List<PlatformWorkbenchTaskVO> selectInvoiceTasks(Integer limit) { return invoiceTasks; }
        @Override public List<PlatformWorkbenchTaskVO> selectMarketReportTasks(Integer limit) { return marketReportTasks; }
    }
}
