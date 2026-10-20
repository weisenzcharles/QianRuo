package com.ruoyi.system.service.rental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.model.PlatformOperationsAnalyticsView;
import com.ruoyi.system.api.model.PlatformOperationsCityStat;
import com.ruoyi.system.api.model.PlatformOperationsMerchantStat;
import com.ruoyi.system.api.model.PlatformOperationsModelStat;
import com.ruoyi.system.api.model.PlatformOperationsTrendView;
import com.ruoyi.system.enums.PlatformOperationsRiskLevel;
import com.ruoyi.system.enums.PlatformOperationsRiskType;
import com.ruoyi.system.enums.PlatformOperationsRiskStatus;
import com.ruoyi.system.mapper.rental.PlatformFinanceMapper;
import com.ruoyi.system.mapper.rental.PlatformAlertEventMapper;
import com.ruoyi.system.mapper.rental.PlatformGovernanceMapper;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.model.rental.PlatformFinanceAccountVO;
import com.ruoyi.system.model.rental.PlatformFinanceQuery;
import com.ruoyi.system.model.rental.PlatformFinanceSummaryVO;
import com.ruoyi.system.model.rental.PlatformMerchantOverviewBase;
import com.ruoyi.system.model.rental.PlatformOperationsMerchantVO;
import com.ruoyi.system.model.rental.PlatformOperationsCityVO;
import com.ruoyi.system.model.rental.PlatformOperationsModelVO;
import com.ruoyi.system.model.rental.PlatformOperationsOverviewVO;
import com.ruoyi.system.model.rental.PlatformOperationsQuery;
import com.ruoyi.system.model.rental.PlatformOperationsRiskVO;
import com.ruoyi.system.model.rental.PlatformOperationsSummaryVO;
import com.ruoyi.system.model.rental.PlatformOperationsTrendVO;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 平台经营分析和运营风控编排服务。
 *
 * <p>订单和库存指标由租赁业务服务按商户范围聚合，平台服务只负责补全商户名称、汇总财务与治理待办，
 * 保证浏览器接口不泄露内部商户唯一编号。</p>
 */
@Service
public class PlatformOperationsService
{
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<Integer> SUPPORTED_DAYS = Set.of(7, 15, 30, 90);
    private static final int MAX_RISK_ROWS = 20;

    private final RentalMemberMapper memberMapper;
    private final RentalBusinessGateway rentalBusinessGateway;
    private final PlatformFinanceService financeService;
    private final PlatformFinanceMapper financeMapper;
    private final PlatformAlertEventMapper alertEventMapper;
    private final PlatformGovernanceMapper governanceMapper;
    private final ISysConfigService configService;

    @Autowired
    public PlatformOperationsService(RentalMemberMapper memberMapper, RentalBusinessGateway rentalBusinessGateway,
            PlatformFinanceService financeService, PlatformFinanceMapper financeMapper,
            PlatformGovernanceMapper governanceMapper, ISysConfigService configService,
            PlatformAlertEventMapper alertEventMapper)
    {
        this.memberMapper = memberMapper;
        this.rentalBusinessGateway = rentalBusinessGateway;
        this.financeService = financeService;
        this.financeMapper = financeMapper;
        this.alertEventMapper = alertEventMapper;
        this.governanceMapper = governanceMapper;
        this.configService = configService;
    }

    /** 保留精简构造器，方便无 Spring 上下文的服务单元测试使用默认风控阈值。 */
    public PlatformOperationsService(RentalMemberMapper memberMapper, RentalBusinessGateway rentalBusinessGateway,
            PlatformFinanceService financeService, PlatformFinanceMapper financeMapper,
            PlatformGovernanceMapper governanceMapper)
    {
        this(memberMapper, rentalBusinessGateway, financeService, financeMapper, governanceMapper, null, null);
    }

    /** 保留带配置服务的构造器，供已有平台经营分析测试和扩展调用使用。 */
    public PlatformOperationsService(RentalMemberMapper memberMapper, RentalBusinessGateway rentalBusinessGateway,
            PlatformFinanceService financeService, PlatformFinanceMapper financeMapper,
            PlatformGovernanceMapper governanceMapper, ISysConfigService configService)
    {
        this(memberMapper, rentalBusinessGateway, financeService, financeMapper, governanceMapper, configService, null);
    }

    /** 查询指定周期的经营趋势、商户排行和可解释风险事项。 */
    public PlatformOperationsOverviewVO overview(PlatformOperationsQuery query)
    {
        int days = normalizeDays(query == null ? null : query.getDays());
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE);
        LocalDate beginDate = endDate.minusDays(days - 1L);
        List<PlatformMerchantOverviewBase> bases = safeMembers(memberMapper.selectAllRentalMemberOverview());
        List<Long> memberIds = new ArrayList<Long>(bases.size());
        Map<Long, PlatformMerchantOverviewBase> baseByMember = new HashMap<Long, PlatformMerchantOverviewBase>();
        for (PlatformMerchantOverviewBase base : bases)
        {
            if (base == null || base.getMemberId() == null)
                continue;
            memberIds.add(base.getMemberId());
            baseByMember.put(base.getMemberId(), base);
        }

        PlatformOperationsAnalyticsView analytics = memberIds.isEmpty()
                ? new PlatformOperationsAnalyticsView()
                : rentalBusinessGateway.getPlatformOperationsAnalytics(memberIds, beginDate.toString(), endDate.toString());
        PlatformFinanceSummaryVO finance = financeService.summary();
        int pendingTickets = governanceMapper.countOpenTickets();
        int pendingWithdrawals = financeMapper.countPendingWithdrawals();
        int pendingAlerts = alertEventMapper == null ? 0 : alertEventMapper.countOpenEvents();
        OperationsRiskThresholds thresholds = loadRiskThresholds();

        PlatformOperationsOverviewVO result = new PlatformOperationsOverviewVO();
        result.setBeginDate(beginDate.toString());
        result.setEndDate(endDate.toString());
        result.setSummary(buildSummary(days, bases.size(), analytics, finance, pendingTickets, pendingWithdrawals,
                thresholds));
        result.setTrends(buildTrends(beginDate, endDate, analytics.getTrends()));
        result.setCityStats(buildCityStats(analytics.getCityStats()));
        result.setModelStats(buildModelStats(analytics.getModelStats()));

        List<PlatformOperationsRiskVO> risks = new ArrayList<PlatformOperationsRiskVO>();
        result.setMerchants(buildMerchants(analytics.getMerchantStats(), baseByMember, risks, thresholds));
        appendGlobalRisks(risks, finance, pendingTickets, pendingWithdrawals, pendingAlerts, thresholds);
        appendDepositRisks(risks, thresholds);
        if (risks.isEmpty())
        {
            risks.add(risk(PlatformOperationsRiskType.WORK_ORDER, PlatformOperationsRiskLevel.NORMAL, "运营检查",
                    0L, "当前周期内未发现待处理运营风险"));
        }
        List<PlatformOperationsRiskVO> sortedRisks = sortRisks(risks);
        synchronizeRiskEvents(sortedRisks);
        result.setRisks(limitRisks(sortedRisks));
        return result;
    }

    /** 查询时同步当前风险观察结果，已消失的风险自动转为已解除。 */
    private void synchronizeRiskEvents(List<PlatformOperationsRiskVO> risks)
    {
        Long operator = SecurityUtils.getUserId();
        List<String> currentKeys = new ArrayList<String>();
        for (PlatformOperationsRiskVO risk : risks)
        {
            if (risk == null || safeInt(risk.getRiskLevel()) == PlatformOperationsRiskLevel.NORMAL.getCode())
                continue;
            String eventKey = riskEventKey(risk);
            currentKeys.add(eventKey);
            if (mapperUpsertRiskEvent(eventKey, risk, operator) <= 0)
                throw new ServiceException("运营风险事件保存失败");
            PlatformOperationsRiskVO stored = governanceMapper.selectRiskEventByKey(eventKey);
            if (stored != null)
            {
                risk.setRiskEventId(stored.getRiskEventId());
                risk.setEventStatus(stored.getEventStatus());
                risk.setEventStatusName(riskStatusName(stored.getEventStatus()));
                risk.setFirstSeenTime(stored.getFirstSeenTime());
                risk.setLastSeenTime(stored.getLastSeenTime());
            }
        }
        governanceMapper.resolveMissingRiskEvents(currentKeys, operator);
    }

    private int mapperUpsertRiskEvent(String eventKey, PlatformOperationsRiskVO risk, Long operator)
    {
        return governanceMapper.upsertRiskEvent(eventKey, risk.getRiskType(), risk.getRiskLevel(), risk.getSubject(),
                Long.valueOf(risk.getCount()), risk.getDetail(), operator);
    }

    /** 确认一条待处理运营风险，保留事件历史和最近发现时间。 */
    public void acknowledgeRisk(Long riskEventId)
    {
        if (riskEventId == null || riskEventId.longValue() <= 0L)
            throw new ServiceException("运营风险事件唯一编号不合法");
        PlatformOperationsRiskVO event = governanceMapper.selectRiskEventById(riskEventId);
        if (event == null)
            throw new ServiceException("运营风险事件不存在或已删除");
        PlatformOperationsRiskStatus status = PlatformOperationsRiskStatus.fromCode(event.getEventStatus());
        if (status == PlatformOperationsRiskStatus.ACKNOWLEDGED)
            return;
        if (status == PlatformOperationsRiskStatus.RESOLVED)
            throw new ServiceException("运营风险事件已经解除");
        if (governanceMapper.acknowledgeRiskEvent(riskEventId, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("运营风险确认失败，请刷新后重试");
    }

    private int normalizeDays(Integer value)
    {
        int days = value == null ? 30 : value.intValue();
        if (!SUPPORTED_DAYS.contains(Integer.valueOf(days)))
            throw new ServiceException("经营分析天数仅支持7天、15天、30天或90天");
        return days;
    }

    private PlatformOperationsSummaryVO buildSummary(int days, int merchantCount,
            PlatformOperationsAnalyticsView analytics, PlatformFinanceSummaryVO finance,
            int pendingTickets, int pendingWithdrawals, OperationsRiskThresholds thresholds)
    {
        PlatformOperationsSummaryVO result = new PlatformOperationsSummaryVO();
        result.setPeriodDays(days);
        result.setMerchantCount(merchantCount);
        long orderCount = 0L;
        long activeOrderCount = 0L;
        long completedOrderCount = 0L;
        long overdueOrderCount = 0L;
        long totalInventoryQuantity = 0L;
        long availableInventoryQuantity = 0L;
        BigDecimal rentAmount = BigDecimal.ZERO;
        long lowInventoryMerchants = 0L;
        for (PlatformOperationsMerchantStat stat : safeStats(analytics.getMerchantStats()))
        {
            orderCount += stat.getOrderCount();
            activeOrderCount += stat.getActiveOrderCount();
            completedOrderCount += stat.getCompletedOrderCount();
            overdueOrderCount += stat.getOverdueOrderCount();
            totalInventoryQuantity += stat.getTotalInventoryQuantity();
            availableInventoryQuantity += stat.getAvailableInventoryQuantity();
            rentAmount = rentAmount.add(decimal(stat.getRentAmount()));
            if (isLowInventory(stat.getTotalInventoryQuantity(), stat.getAvailableInventoryQuantity(),
                    thresholds.lowInventoryPercent))
                lowInventoryMerchants++;
        }
        result.setOrderCount(orderCount);
        result.setRentAmount(rentAmount);
        result.setActiveOrderCount(activeOrderCount);
        result.setCompletedOrderCount(completedOrderCount);
        result.setOverdueOrderCount(overdueOrderCount);
        result.setTotalInventoryQuantity(totalInventoryQuantity);
        result.setAvailableInventoryQuantity(availableInventoryQuantity);
        result.setLowInventoryMerchantCount(lowInventoryMerchants);
        result.setTotalIncome(decimal(finance == null ? null : finance.getTotalIncome()));
        result.setTotalExpense(decimal(finance == null ? null : finance.getTotalExpense()));
        result.setNetAmount(decimal(finance == null ? null : finance.getNetAmount()));
        result.setPendingTicketCount(Math.max(pendingTickets, 0));
        result.setPendingWithdrawalCount(Math.max(pendingWithdrawals, 0));
        result.setAbnormalLedgerCount(finance == null || finance.getAbnormalLedgerCount() == null
                ? 0 : Math.max(finance.getAbnormalLedgerCount().intValue(), 0));
        return result;
    }

    private List<PlatformOperationsTrendVO> buildTrends(LocalDate beginDate, LocalDate endDate,
            List<PlatformOperationsTrendView> source)
    {
        Map<String, PlatformOperationsTrendView> sourceByDate = new HashMap<String, PlatformOperationsTrendView>();
        for (PlatformOperationsTrendView row : safeTrends(source))
        {
            if (row != null && row.getStatDate() != null)
                sourceByDate.put(row.getStatDate(), row);
        }
        List<PlatformOperationsTrendVO> result = new ArrayList<PlatformOperationsTrendVO>();
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1L))
        {
            String dateText = date.toString();
            PlatformOperationsTrendView sourceRow = sourceByDate.get(dateText);
            PlatformOperationsTrendVO row = new PlatformOperationsTrendVO();
            row.setStatDate(dateText);
            if (sourceRow != null)
            {
                row.setOrderCount(sourceRow.getOrderCount());
                row.setRentAmount(decimal(sourceRow.getRentAmount()));
                row.setCompletedOrderCount(sourceRow.getCompletedOrderCount());
                row.setOverdueOrderCount(sourceRow.getOverdueOrderCount());
            }
            result.add(row);
        }
        return result;
    }

    private List<PlatformOperationsMerchantVO> buildMerchants(List<PlatformOperationsMerchantStat> source,
            Map<Long, PlatformMerchantOverviewBase> baseByMember, List<PlatformOperationsRiskVO> risks,
            OperationsRiskThresholds thresholds)
    {
        List<PlatformOperationsMerchantVO> result = new ArrayList<PlatformOperationsMerchantVO>();
        for (PlatformOperationsMerchantStat stat : safeStats(source))
        {
            if (stat == null || stat.getMemberId() == null)
                continue;
            PlatformMerchantOverviewBase base = baseByMember.get(stat.getMemberId());
            if (base == null)
                continue;
            PlatformOperationsMerchantVO row = new PlatformOperationsMerchantVO();
            row.setMemberCode(text(base.getMemberCode(), "-"));
            row.setMerchantName(merchantName(base));
            row.setOrderCount(stat.getOrderCount());
            row.setRentAmount(decimal(stat.getRentAmount()));
            row.setActiveOrderCount(stat.getActiveOrderCount());
            row.setCompletedOrderCount(stat.getCompletedOrderCount());
            row.setOverdueOrderCount(stat.getOverdueOrderCount());
            row.setTotalInventoryQuantity(stat.getTotalInventoryQuantity());
            row.setAvailableInventoryQuantity(stat.getAvailableInventoryQuantity());
            result.add(row);
            appendMerchantRisks(risks, row, thresholds);
        }
        result.sort(Comparator.comparingLong(PlatformOperationsMerchantVO::getOrderCount).reversed()
                .thenComparing(PlatformOperationsMerchantVO::getMerchantName));
        return result;
    }

    private List<PlatformOperationsCityVO> buildCityStats(List<PlatformOperationsCityStat> source)
    {
        List<PlatformOperationsCityVO> result = new ArrayList<PlatformOperationsCityVO>();
        for (PlatformOperationsCityStat stat : safeCityStats(source))
        {
            if (stat == null)
                continue;
            PlatformOperationsCityVO row = new PlatformOperationsCityVO();
            row.setCityName(text(stat.getCityName(), "未解析城市"));
            row.setOrderCount(stat.getOrderCount());
            row.setRentAmount(decimal(stat.getRentAmount()));
            row.setCompletedOrderCount(stat.getCompletedOrderCount());
            row.setOverdueOrderCount(stat.getOverdueOrderCount());
            result.add(row);
        }
        return result;
    }

    private List<PlatformOperationsModelVO> buildModelStats(List<PlatformOperationsModelStat> source)
    {
        List<PlatformOperationsModelVO> result = new ArrayList<PlatformOperationsModelVO>();
        for (PlatformOperationsModelStat stat : safeModelStats(source))
        {
            if (stat == null)
                continue;
            PlatformOperationsModelVO row = new PlatformOperationsModelVO();
            row.setModelName(text(stat.getModelName(), "未指定型号"));
            row.setOrderCount(stat.getOrderCount());
            row.setQuantity(stat.getQuantity());
            row.setCompletedOrderCount(stat.getCompletedOrderCount());
            row.setOverdueOrderCount(stat.getOverdueOrderCount());
            result.add(row);
        }
        return result;
    }

    private void appendMerchantRisks(List<PlatformOperationsRiskVO> risks, PlatformOperationsMerchantVO merchant,
            OperationsRiskThresholds thresholds)
    {
        if (merchant.getOverdueOrderCount() > 0L)
        {
            PlatformOperationsRiskLevel level = merchant.getOverdueOrderCount() >= thresholds.overdueOrderCount
                    ? PlatformOperationsRiskLevel.HIGH : PlatformOperationsRiskLevel.WARNING;
            risks.add(risk(PlatformOperationsRiskType.ORDER, level, merchant.getMerchantName(),
                    merchant.getOverdueOrderCount(), "存在" + merchant.getOverdueOrderCount() + "笔逾期订单，请及时跟进"));
        }
        if (merchant.getTotalInventoryQuantity() > 0L
                && merchant.getAvailableInventoryQuantity() * 100L
                        <= merchant.getTotalInventoryQuantity() * thresholds.lowInventoryPercent)
        {
            PlatformOperationsRiskLevel level = merchant.getAvailableInventoryQuantity() <= 0L
                    ? PlatformOperationsRiskLevel.HIGH : PlatformOperationsRiskLevel.WARNING;
            risks.add(risk(PlatformOperationsRiskType.INVENTORY, level, merchant.getMerchantName(),
                    merchant.getAvailableInventoryQuantity(), "可用库存仅占总库存" + inventoryRate(merchant) + "，请检查补货和占用情况"));
        }
    }

    private void appendGlobalRisks(List<PlatformOperationsRiskVO> risks, PlatformFinanceSummaryVO finance,
            int pendingTickets, int pendingWithdrawals, int pendingAlerts, OperationsRiskThresholds thresholds)
    {
        int abnormalLedgerCount = finance == null || finance.getAbnormalLedgerCount() == null
                ? 0 : Math.max(finance.getAbnormalLedgerCount().intValue(), 0);
        if (abnormalLedgerCount > 0)
        {
            risks.add(risk(PlatformOperationsRiskType.FINANCE, PlatformOperationsRiskLevel.HIGH, "平台财务流水",
                    abnormalLedgerCount, "发现" + abnormalLedgerCount + "条异常流水，请核查财务状态"));
        }
        if (pendingWithdrawals >= thresholds.pendingWithdrawalCount)
        {
            risks.add(risk(PlatformOperationsRiskType.FINANCE, PlatformOperationsRiskLevel.WARNING, "商户提现审核",
                    pendingWithdrawals, "有" + pendingWithdrawals + "笔提现申请待审核"));
        }
        if (pendingTickets >= thresholds.pendingTicketCount)
        {
            risks.add(risk(PlatformOperationsRiskType.WORK_ORDER, PlatformOperationsRiskLevel.NOTICE, "平台客服工单",
                    pendingTickets, "有" + pendingTickets + "个工单尚未关闭"));
        }
        if (pendingAlerts > 0)
        {
            risks.add(risk(PlatformOperationsRiskType.SYSTEM, PlatformOperationsRiskLevel.WARNING, "平台系统告警",
                    pendingAlerts, "有" + pendingAlerts + "条系统告警尚未解除，请进入系统告警处理"));
        }
    }

    /** 按商户履约保证金可用余额生成风险事项，冻结金额不能抵扣最低可用阈值。 */
    private void appendDepositRisks(List<PlatformOperationsRiskVO> risks, OperationsRiskThresholds thresholds)
    {
        if (thresholds.minimumPerformanceDeposit.signum() <= 0 || financeMapper == null)
            return;
        List<PlatformFinanceAccountVO> accounts = financeMapper.selectAccounts(new PlatformFinanceQuery());
        if (accounts == null)
            return;
        for (PlatformFinanceAccountVO account : accounts)
        {
            if (account == null || decimal(account.getPerformanceDepositAvailableAmount())
                    .compareTo(thresholds.minimumPerformanceDeposit) >= 0)
                continue;
            String merchantName = text(account.getMerchantName(), text(account.getLegalSubjectName(),
                    text(account.getMemberCode(), "未命名商户")));
            BigDecimal available = decimal(account.getPerformanceDepositAvailableAmount());
            PlatformOperationsRiskLevel level = available.signum() <= 0
                    ? PlatformOperationsRiskLevel.HIGH : PlatformOperationsRiskLevel.WARNING;
            risks.add(risk(PlatformOperationsRiskType.FINANCE, level, merchantName, 1L,
                    "履约保证金可用余额 " + money(available) + " 元，低于平台阈值 "
                            + money(thresholds.minimumPerformanceDeposit) + " 元"));
        }
    }

    private OperationsRiskThresholds loadRiskThresholds()
    {
        OperationsRiskThresholds thresholds = new OperationsRiskThresholds();
        thresholds.lowInventoryPercent = configInteger("platform.operations.risk.low-inventory-percent",
                thresholds.lowInventoryPercent, 1, 100);
        thresholds.overdueOrderCount = configInteger("platform.operations.risk.overdue-order-count",
                thresholds.overdueOrderCount, 1, Integer.MAX_VALUE);
        thresholds.pendingTicketCount = configInteger("platform.operations.risk.pending-ticket-count",
                thresholds.pendingTicketCount, 1, Integer.MAX_VALUE);
        thresholds.pendingWithdrawalCount = configInteger("platform.operations.risk.pending-withdrawal-count",
                thresholds.pendingWithdrawalCount, 1, Integer.MAX_VALUE);
        thresholds.minimumPerformanceDeposit = configDecimal("platform.operations.risk.minimum-performance-deposit",
                thresholds.minimumPerformanceDeposit);
        return thresholds;
    }

    private int configInteger(String key, int defaultValue, int min, int max)
    {
        if (configService == null)
            return defaultValue;
        String value = configService.selectConfigByKey(key);
        if (value == null || value.trim().isEmpty())
            return defaultValue;
        try
        {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= min && parsed <= max ? parsed : defaultValue;
        }
        catch (NumberFormatException exception)
        {
            return defaultValue;
        }
    }

    private BigDecimal configDecimal(String key, BigDecimal defaultValue)
    {
        if (configService == null)
            return defaultValue;
        String value = configService.selectConfigByKey(key);
        if (value == null || value.trim().isEmpty())
            return defaultValue;
        try
        {
            BigDecimal parsed = new BigDecimal(value.trim());
            return parsed.signum() >= 0 ? parsed : defaultValue;
        }
        catch (NumberFormatException exception)
        {
            return defaultValue;
        }
    }

    private List<PlatformOperationsRiskVO> sortRisks(List<PlatformOperationsRiskVO> source)
    {
        source.sort((left, right) -> {
            int levelCompare = Integer.compare(safeInt(right.getRiskLevel()), safeInt(left.getRiskLevel()));
            return levelCompare != 0 ? levelCompare : Long.compare(right.getCount(), left.getCount());
        });
        return source;
    }

    private List<PlatformOperationsRiskVO> limitRisks(List<PlatformOperationsRiskVO> source)
    {
        return source.size() <= MAX_RISK_ROWS ? source : new ArrayList<PlatformOperationsRiskVO>(source.subList(0,
                MAX_RISK_ROWS));
    }

    private PlatformOperationsRiskVO risk(PlatformOperationsRiskType type, PlatformOperationsRiskLevel level,
            String subject, long count, String detail)
    {
        PlatformOperationsRiskVO result = new PlatformOperationsRiskVO();
        result.setRiskType(type.getCode());
        result.setRiskTypeName(type.getLabel());
        result.setRiskLevel(level.getCode());
        result.setRiskLevelName(level.getLabel());
        result.setSubject(subject);
        result.setCount(count);
        result.setDetail(detail);
        result.setEventStatus(PlatformOperationsRiskStatus.PENDING.getCode());
        result.setEventStatusName(PlatformOperationsRiskStatus.PENDING.getLabel());
        return result;
    }

    private String riskEventKey(PlatformOperationsRiskVO risk)
    {
        return String.valueOf(risk.getRiskType()) + ":" + text(risk.getSubject(), "未知主体");
    }

    private String riskStatusName(Integer status)
    {
        PlatformOperationsRiskStatus value = PlatformOperationsRiskStatus.fromCode(status);
        return value == null ? "未知状态" : value.getLabel();
    }

    private boolean isLowInventory(long total, long available, int lowInventoryPercent)
    {
        return total > 0L && available * 100L <= total * lowInventoryPercent;
    }

    private String inventoryRate(PlatformOperationsMerchantVO merchant)
    {
        if (merchant.getTotalInventoryQuantity() <= 0L)
            return "0%";
        long rate = merchant.getAvailableInventoryQuantity() * 100L / merchant.getTotalInventoryQuantity();
        return rate + "%";
    }

    private String money(BigDecimal value)
    {
        return decimal(value).stripTrailingZeros().toPlainString();
    }

    private static final class OperationsRiskThresholds
    {
        private int lowInventoryPercent = 10;
        private int overdueOrderCount = 3;
        private int pendingTicketCount = 10;
        private int pendingWithdrawalCount = 10;
        private BigDecimal minimumPerformanceDeposit = BigDecimal.ZERO;
    }

    private List<PlatformMerchantOverviewBase> safeMembers(List<PlatformMerchantOverviewBase> source)
    {
        return source == null ? Collections.emptyList() : source;
    }

    private List<PlatformOperationsMerchantStat> safeStats(List<PlatformOperationsMerchantStat> source)
    {
        return source == null ? Collections.emptyList() : source;
    }

    private List<PlatformOperationsTrendView> safeTrends(List<PlatformOperationsTrendView> source)
    {
        return source == null ? Collections.emptyList() : source;
    }

    private List<PlatformOperationsCityStat> safeCityStats(List<PlatformOperationsCityStat> source)
    {
        return source == null ? Collections.emptyList() : source;
    }

    private List<PlatformOperationsModelStat> safeModelStats(List<PlatformOperationsModelStat> source)
    {
        return source == null ? Collections.emptyList() : source;
    }

    private BigDecimal decimal(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int safeInt(Integer value)
    {
        return value == null ? 0 : value.intValue();
    }

    private String merchantName(PlatformMerchantOverviewBase base)
    {
        return text(base.getNickname(), text(base.getLegalSubjectName(), text(base.getMemberCode(), "未命名商户")));
    }

    private String text(String value, String defaultValue)
    {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
