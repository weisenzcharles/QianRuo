package com.ruoyi.system.rental;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.api.RemoteRentalBusinessService;
import com.ruoyi.system.api.model.PlatformOperationsAnalyticsView;
import com.ruoyi.system.api.model.PlatformOperationsCityStat;
import com.ruoyi.system.api.model.PlatformOperationsMerchantStat;
import com.ruoyi.system.api.model.PlatformOperationsModelStat;
import com.ruoyi.system.mapper.rental.PlatformFinanceMapper;
import com.ruoyi.system.mapper.rental.PlatformAlertEventMapper;
import com.ruoyi.system.mapper.rental.PlatformGovernanceMapper;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.model.rental.PlatformFinanceAccountVO;
import com.ruoyi.system.model.rental.PlatformMerchantOverviewBase;
import com.ruoyi.system.model.rental.PlatformOperationsOverviewVO;
import com.ruoyi.system.model.rental.PlatformOperationsQuery;
import com.ruoyi.system.model.rental.PlatformOperationsRiskVO;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.rental.PlatformFinanceService;
import com.ruoyi.system.service.rental.PlatformOperationsService;
import com.ruoyi.system.service.rental.RentalBusinessGateway;

/** 平台经营分析的周期校验、中文风险标签和商户名称组装测试。 */
public class PlatformOperationsServiceTest
{
    @Test
    public void overview_rejectsUnsupportedDaysBeforeQuery()
    {
        PlatformOperationsQuery query = new PlatformOperationsQuery();
        query.setDays(Integer.valueOf(10));
        try
        {
            new PlatformOperationsService(null, null, null, null, null).overview(query);
            Assert.fail("unsupported days must be rejected");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("经营分析天数仅支持7天、15天、30天或90天", expected.getMessage());
        }
    }

    @Test
    public void overview_assemblesMerchantNamesAndRiskLabels()
    {
        RentalMemberMapper memberMapper = (RentalMemberMapper) proxy(RentalMemberMapper.class,
                (method, args) -> "selectAllRentalMemberOverview".equals(method) ? Collections.singletonList(member()) : null);
        PlatformFinanceMapper financeMapper = (PlatformFinanceMapper) proxy(PlatformFinanceMapper.class,
                (method, args) -> {
                    if ("selectSummary".equals(method))
                    {
                        Map<String, Object> summary = new HashMap<String, Object>();
                        summary.put("abnormalLedgerCount", Integer.valueOf(2));
                        return summary;
                    }
                    if ("countPendingWithdrawals".equals(method)) return Integer.valueOf(1);
                    return null;
                });
        PlatformGovernanceMapper governanceMapper = (PlatformGovernanceMapper) proxy(PlatformGovernanceMapper.class,
                (method, args) -> {
                    if ("countOpenTickets".equals(method)) return Integer.valueOf(1);
                    if ("upsertRiskEvent".equals(method)) return Integer.valueOf(1);
                    if ("resolveMissingRiskEvents".equals(method)) return Integer.valueOf(1);
                    if ("selectRiskEventByKey".equals(method)) return storedRiskEvent();
                    return null;
                });
        RemoteRentalBusinessService remote = (RemoteRentalBusinessService) proxy(RemoteRentalBusinessService.class,
                (method, args) -> {
                    if (!"getPlatformOperationsAnalytics".equals(method)) return null;
                    PlatformOperationsAnalyticsView view = new PlatformOperationsAnalyticsView();
                    PlatformOperationsMerchantStat stat = new PlatformOperationsMerchantStat();
                    stat.setMemberId(Long.valueOf(27L));
                    stat.setOrderCount(3L);
                    stat.setRentAmount(new BigDecimal("120.00"));
                    stat.setOverdueOrderCount(1L);
                    stat.setTotalInventoryQuantity(10L);
                    stat.setAvailableInventoryQuantity(0L);
                    view.setMerchantStats(Collections.singletonList(stat));
                    PlatformOperationsCityStat city = new PlatformOperationsCityStat();
                    city.setCityName("杭州市");
                    city.setOrderCount(2L);
                    view.setCityStats(Collections.singletonList(city));
                    PlatformOperationsModelStat model = new PlatformOperationsModelStat();
                    model.setModelName("EOS R50");
                    model.setQuantity(2L);
                    view.setModelStats(Collections.singletonList(model));
                    return R.ok(view);
                });

        PlatformOperationsService service = new PlatformOperationsService(memberMapper,
                new RentalBusinessGateway(remote), new PlatformFinanceService(financeMapper, null), financeMapper,
                governanceMapper);
        PlatformOperationsOverviewVO result = service.overview(new PlatformOperationsQuery());

        Assert.assertEquals("测试商户", result.getMerchants().get(0).getMerchantName());
        Assert.assertEquals(Long.valueOf(3L), Long.valueOf(result.getSummary().getOrderCount()));
        Assert.assertEquals("杭州市", result.getCityStats().get(0).getCityName());
        Assert.assertEquals("EOS R50", result.getModelStats().get(0).getModelName());
        Assert.assertTrue(result.getRisks().stream().anyMatch(row -> "订单".equals(row.getRiskTypeName())
                && "预警".equals(row.getRiskLevelName())));
        Assert.assertTrue(result.getRisks().stream().anyMatch(row -> "财务".equals(row.getRiskTypeName())
                && "高风险".equals(row.getRiskLevelName())));
        Assert.assertTrue(result.getRisks().stream().allMatch(row -> row.getRiskEventId() != null));
    }

    @Test
    public void overview_usesConfiguredRiskThresholdsAndDeposit()
    {
        RentalMemberMapper memberMapper = (RentalMemberMapper) proxy(RentalMemberMapper.class,
                (method, args) -> "selectAllRentalMemberOverview".equals(method) ? Collections.singletonList(member()) : null);
        PlatformFinanceAccountVO account = new PlatformFinanceAccountVO();
        account.setMerchantName("保证金商户");
        account.setPerformanceDepositAvailableAmount(new BigDecimal("500.00"));
        PlatformFinanceMapper financeMapper = (PlatformFinanceMapper) proxy(PlatformFinanceMapper.class,
                (method, args) -> {
                    if ("selectSummary".equals(method))
                    {
                        Map<String, Object> summary = new HashMap<String, Object>();
                        summary.put("abnormalLedgerCount", Integer.valueOf(0));
                        return summary;
                    }
                    if ("countPendingWithdrawals".equals(method)) return Integer.valueOf(2);
                    if ("selectAccounts".equals(method)) return Collections.singletonList(account);
                    return null;
                });
        PlatformGovernanceMapper governanceMapper = (PlatformGovernanceMapper) proxy(PlatformGovernanceMapper.class,
                (method, args) -> {
                    if ("countOpenTickets".equals(method)) return Integer.valueOf(2);
                    if ("upsertRiskEvent".equals(method)) return Integer.valueOf(1);
                    if ("resolveMissingRiskEvents".equals(method)) return Integer.valueOf(1);
                    if ("selectRiskEventByKey".equals(method)) return storedRiskEvent();
                    return null;
                });
        RemoteRentalBusinessService remote = (RemoteRentalBusinessService) proxy(RemoteRentalBusinessService.class,
                (method, args) -> {
                    if (!"getPlatformOperationsAnalytics".equals(method)) return null;
                    PlatformOperationsAnalyticsView view = new PlatformOperationsAnalyticsView();
                    PlatformOperationsMerchantStat stat = new PlatformOperationsMerchantStat();
                    stat.setMemberId(Long.valueOf(27L));
                    stat.setOverdueOrderCount(1L);
                    stat.setTotalInventoryQuantity(10L);
                    stat.setAvailableInventoryQuantity(2L);
                    view.setMerchantStats(Collections.singletonList(stat));
                    return R.ok(view);
                });
        Map<String, String> values = new HashMap<String, String>();
        values.put("platform.operations.risk.low-inventory-percent", "20");
        values.put("platform.operations.risk.overdue-order-count", "1");
        values.put("platform.operations.risk.pending-ticket-count", "2");
        values.put("platform.operations.risk.pending-withdrawal-count", "2");
        values.put("platform.operations.risk.minimum-performance-deposit", "1000");
        ISysConfigService configService = (ISysConfigService) proxy(ISysConfigService.class,
                (method, args) -> "selectConfigByKey".equals(method) ? values.get(args[0]) : null);

        PlatformOperationsService service = new PlatformOperationsService(memberMapper,
                new RentalBusinessGateway(remote), new PlatformFinanceService(financeMapper, null), financeMapper,
                governanceMapper, configService,
                (PlatformAlertEventMapper) proxy(PlatformAlertEventMapper.class,
                        (method, args) -> "countOpenEvents".equals(method) ? Integer.valueOf(2) : null));
        PlatformOperationsOverviewVO result = service.overview(new PlatformOperationsQuery());

        Assert.assertEquals(Long.valueOf(1L), Long.valueOf(result.getSummary().getLowInventoryMerchantCount()));
        Assert.assertTrue(result.getRisks().stream().anyMatch(row -> "订单".equals(row.getRiskTypeName())
                && "高风险".equals(row.getRiskLevelName())));
        Assert.assertTrue(result.getRisks().stream().anyMatch(row -> "保证金商户".equals(row.getSubject())
                && row.getDetail().contains("1000")));
        Assert.assertTrue(result.getRisks().stream().anyMatch(row -> "商户提现审核".equals(row.getSubject())));
        Assert.assertTrue(result.getRisks().stream().anyMatch(row -> "平台客服工单".equals(row.getSubject())));
        Assert.assertTrue(result.getRisks().stream().anyMatch(row -> "平台系统告警".equals(row.getSubject())
                && "系统".equals(row.getRiskTypeName()) && row.getCount() == 2L));
        Assert.assertTrue(result.getRisks().stream().noneMatch(row -> "运营检查".equals(row.getSubject())));
    }

    @Test
    public void acknowledgeRisk_待处理事件_更新为已确认()
    {
        PlatformGovernanceMapper governanceMapper = (PlatformGovernanceMapper) proxy(PlatformGovernanceMapper.class,
                (method, args) -> {
                    if ("selectRiskEventById".equals(method)) return storedRiskEvent();
                    if ("acknowledgeRiskEvent".equals(method)) return Integer.valueOf(1);
                    return null;
                });
        PlatformOperationsService service = new PlatformOperationsService(null, null, null, null, governanceMapper);
        service.acknowledgeRisk(Long.valueOf(9L));
    }

    private PlatformOperationsRiskVO storedRiskEvent()
    {
        PlatformOperationsRiskVO event = new PlatformOperationsRiskVO();
        event.setRiskEventId(Long.valueOf(9L));
        event.setEventStatus(Integer.valueOf(0));
        event.setFirstSeenTime("2026-09-26 00:00:00");
        event.setLastSeenTime("2026-09-26 00:00:00");
        return event;
    }

    private PlatformMerchantOverviewBase member()
    {
        PlatformMerchantOverviewBase member = new PlatformMerchantOverviewBase();
        member.setMemberId(Long.valueOf(27L));
        member.setMemberCode("M00000000027");
        member.setNickname("测试商户");
        return member;
    }

    private Object proxy(Class<?> type, MapperCall call)
    {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (proxy, method, args) -> call.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
