package com.ruoyi.system.service.rental;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.enums.PlatformOperationsRiskStatus;
import com.ruoyi.system.enums.PlatformMarketReportStatus;
import com.ruoyi.system.enums.PlatformTicketPriority;
import com.ruoyi.system.enums.PlatformTicketStatus;
import com.ruoyi.system.enums.PlatformWorkbenchTaskStatus;
import com.ruoyi.system.enums.PlatformWorkbenchTaskType;
import com.ruoyi.system.mapper.rental.PlatformWorkbenchMapper;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchTaskView;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchView;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalStatus;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationStatus;
import com.ruoyi.system.model.rental.PlatformWorkbenchOverviewVO;
import com.ruoyi.system.model.rental.PlatformWorkbenchTaskVO;

/** 平台通用工作台应用服务，汇总需要运营人员跟进的实时待办。 */
@Service
public class PlatformWorkbenchService
{
    private static final int TASK_LIMIT = 50;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(PlatformWorkbenchService.class);

    private final PlatformWorkbenchMapper mapper;
    private final RentalBusinessGateway rentalBusinessGateway;

    /** 注入平台本地待办查询和租赁执行面只读聚合网关。 */
    @Autowired
    public PlatformWorkbenchService(PlatformWorkbenchMapper mapper, RentalBusinessGateway rentalBusinessGateway)
    {
        this.mapper = mapper;
        this.rentalBusinessGateway = rentalBusinessGateway;
    }

    /** 保留本地单元测试和不需要执行面数据的调用方构造方式。 */
    public PlatformWorkbenchService(PlatformWorkbenchMapper mapper)
    {
        this(mapper, null);
    }

    /** 查询平台治理与租赁执行面待办，并按优先级和进入时间统一排序。 */
    public PlatformWorkbenchOverviewVO overview()
    {
        if (mapper == null)
            throw new ServiceException("平台工作台暂不可用");
        int riskCount = mapper.countOpenRiskTasks();
        int ticketCount = mapper.countOpenTicketTasks();
        int withdrawalCount = mapper.countPendingWithdrawalTasks();
        int invoiceCount = mapper.countOpenInvoiceTasks();
        int marketReportCount = mapper.countOpenMarketReportTasks();
        List<PlatformWorkbenchTaskVO> tasks = new ArrayList<PlatformWorkbenchTaskVO>();
        append(tasks, mapper.selectRiskTasks(TASK_LIMIT));
        append(tasks, mapper.selectTicketTasks(TASK_LIMIT));
        append(tasks, mapper.selectWithdrawalTasks(TASK_LIMIT));
        append(tasks, mapper.selectInvoiceTasks(TASK_LIMIT));
        append(tasks, mapper.selectMarketReportTasks(TASK_LIMIT));

        PlatformExecutionWorkbenchView execution = loadExecutionWorkbench();
        int fulfillmentCount = execution == null ? 0 : value(execution.getFulfillmentTaskCount());
        int afterSalesCount = execution == null ? 0 : value(execution.getAfterSalesTaskCount());
        int logisticsCount = execution == null ? 0 : value(execution.getLogisticsTaskCount());
        int inventoryAlertCount = execution == null ? 0 : value(execution.getInventoryAlertTaskCount());
        if (execution != null)
        {
            appendExecution(tasks, execution.getTasks());
        }
        for (PlatformWorkbenchTaskVO task : tasks)
            decorate(task);
        tasks.sort(Comparator.comparing(PlatformWorkbenchTaskVO::getPriority, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(PlatformWorkbenchTaskVO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PlatformWorkbenchTaskVO::getTaskId, Comparator.nullsLast(Comparator.naturalOrder())));
        if (tasks.size() > TASK_LIMIT)
            tasks = new ArrayList<PlatformWorkbenchTaskVO>(tasks.subList(0, TASK_LIMIT));

        PlatformWorkbenchOverviewVO result = new PlatformWorkbenchOverviewVO();
        result.setGeneratedAt(LocalDateTime.now().format(TIME_FORMATTER));
        result.setTotalTaskCount(Integer.valueOf(riskCount + ticketCount + withdrawalCount + invoiceCount + marketReportCount
                + fulfillmentCount + afterSalesCount + logisticsCount + inventoryAlertCount));
        result.setRiskTaskCount(Integer.valueOf(riskCount));
        result.setTicketTaskCount(Integer.valueOf(ticketCount));
        result.setWithdrawalTaskCount(Integer.valueOf(withdrawalCount));
        result.setInvoiceTaskCount(Integer.valueOf(invoiceCount));
        result.setMarketReportTaskCount(Integer.valueOf(marketReportCount));
        result.setFulfillmentTaskCount(Integer.valueOf(fulfillmentCount));
        result.setAfterSalesTaskCount(Integer.valueOf(afterSalesCount));
        result.setLogisticsTaskCount(Integer.valueOf(logisticsCount));
        result.setInventoryAlertTaskCount(Integer.valueOf(inventoryAlertCount));
        result.setExecutionAvailable(Boolean.valueOf(execution != null));
        result.setPendingWithdrawalAmount(defaultAmount(mapper.sumPendingWithdrawalAmount()));
        result.setTasks(tasks);
        return result;
    }

    private PlatformExecutionWorkbenchView loadExecutionWorkbench()
    {
        if (rentalBusinessGateway == null)
        {
            return null;
        }
        try
        {
            PlatformExecutionWorkbenchView result = rentalBusinessGateway.getPlatformExecutionWorkbench();
            return result != null && Boolean.TRUE.equals(result.getAvailable()) ? result : null;
        }
        catch (ServiceException exception)
        {
            log.warn("平台工作台暂时无法读取租赁执行面待办: {}", exception.getMessage());
            return null;
        }
    }

    private void appendExecution(List<PlatformWorkbenchTaskVO> target, List<PlatformExecutionWorkbenchTaskView> source)
    {
        if (source == null)
        {
            return;
        }
        for (PlatformExecutionWorkbenchTaskView sourceTask : source)
        {
            if (sourceTask == null)
            {
                continue;
            }
            PlatformWorkbenchTaskVO task = new PlatformWorkbenchTaskVO();
            task.setTaskId(sourceTask.getTaskId());
            task.setTaskType(sourceTask.getTaskType());
            task.setPriority(sourceTask.getPriority());
            task.setTaskStatus(sourceTask.getTaskStatus());
            task.setTaskStatusName(sourceTask.getTaskStatusName());
            task.setSubject(sourceTask.getSubject());
            task.setDetail(sourceTask.getDetail());
            task.setBusinessNo(sourceTask.getBusinessNo());
            task.setMerchantName(sourceTask.getMerchantName());
            task.setCreateTime(sourceTask.getCreateTime());
            task.setUpdateTime(sourceTask.getUpdateTime());
            task.setTargetPath(targetPath(PlatformWorkbenchTaskType.fromCode(sourceTask.getTaskType()),
                    sourceTask.getBusinessNo(), sourceTask.getMerchantCode()));
            target.add(task);
        }
    }

    private void append(List<PlatformWorkbenchTaskVO> target, List<PlatformWorkbenchTaskVO> source)
    {
        if (source != null)
            target.addAll(source);
    }

    private void decorate(PlatformWorkbenchTaskVO task)
    {
        if (task == null)
            return;
        PlatformWorkbenchTaskType type = PlatformWorkbenchTaskType.fromCode(task.getTaskType());
        task.setTaskTypeName(type == null ? "其他待办" : type.getLabel());
        PlatformTicketPriority priority = PlatformTicketPriority.fromCode(task.getPriority());
        task.setPriorityName(priority == null ? "普通" : priority.getLabel());
        if (!isExecutionTask(type) || task.getTaskStatusName() == null || task.getTaskStatusName().trim().isEmpty())
            task.setTaskStatusName(statusName(type, task.getTaskStatus()));
        if (task.getTargetPath() == null || task.getTargetPath().trim().isEmpty())
            task.setTargetPath(targetPath(type, task.getBusinessNo(), null));
    }

    private boolean isExecutionTask(PlatformWorkbenchTaskType type)
    {
        return type == PlatformWorkbenchTaskType.FULFILLMENT || type == PlatformWorkbenchTaskType.AFTER_SALES
                || type == PlatformWorkbenchTaskType.LOGISTICS || type == PlatformWorkbenchTaskType.INVENTORY_ALERT;
    }

    private String statusName(PlatformWorkbenchTaskType type, Integer status)
    {
        if (type == PlatformWorkbenchTaskType.RISK)
        {
            PlatformOperationsRiskStatus value = PlatformOperationsRiskStatus.fromCode(status);
            return value == null ? "未知状态" : value.getLabel();
        }
        if (type == PlatformWorkbenchTaskType.TICKET)
        {
            PlatformTicketStatus value = status != null && status.intValue() == PlatformWorkbenchTaskStatus.PENDING.getCode()
                    ? PlatformTicketStatus.UNASSIGNED : PlatformTicketStatus.PROCESSING;
            return value.getLabel();
        }
        if (type == PlatformWorkbenchTaskType.INVOICE)
        {
            PlatformInvoiceApplicationStatus value = PlatformInvoiceApplicationStatus.fromCode(status);
            return value == null ? "未知状态" : value.getLabel();
        }
        if (type == PlatformWorkbenchTaskType.MARKET_REPORT)
        {
            PlatformMarketReportStatus value = PlatformMarketReportStatus.fromCode(status);
            return value == null ? "未知状态" : value.getLabel();
        }
        PlatformFinanceWithdrawalStatus value = PlatformFinanceWithdrawalStatus.fromCode(status);
        return value == null ? "未知状态" : value.getLabel();
    }

    private String targetPath(PlatformWorkbenchTaskType type, String businessNo, String merchantCode)
    {
        String encodedBusinessNo = businessNo == null ? "" : URLEncoder.encode(businessNo, StandardCharsets.UTF_8);
        if (type == PlatformWorkbenchTaskType.RISK)
            return businessNo == null || businessNo.trim().isEmpty()
                    ? "/operations/analytics"
                    : "/operations/analytics?riskEventId=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.TICKET)
            return "/governance/tickets?ticketNo=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.WITHDRAWAL)
            return "/finance/finance/withdrawal?withdrawalNo=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.INVOICE)
            return "/finance/finance/invoice?applicationNo=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.MARKET_REPORT)
            return "/operations/market?tab=reports&keyword=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.FULFILLMENT)
            return "/order-center/orders?orderNo=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.AFTER_SALES)
            return "/governance/after-sales?caseNo=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.LOGISTICS)
            return "/governance/logistics?trackingNo=" + encodedBusinessNo;
        if (type == PlatformWorkbenchTaskType.INVENTORY_ALERT)
        {
            if (merchantCode != null && !merchantCode.trim().isEmpty())
                return "/rental/merchant-overview?memberCode="
                        + URLEncoder.encode(merchantCode.trim(), StandardCharsets.UTF_8);
            return "/rental/merchant-overview";
        }
        return "/rental/index";
    }

    private int value(Integer value)
    {
        return value == null ? 0 : value.intValue();
    }

    private BigDecimal defaultAmount(BigDecimal amount)
    {
        return amount == null ? BigDecimal.ZERO.setScale(2) : amount;
    }
}
