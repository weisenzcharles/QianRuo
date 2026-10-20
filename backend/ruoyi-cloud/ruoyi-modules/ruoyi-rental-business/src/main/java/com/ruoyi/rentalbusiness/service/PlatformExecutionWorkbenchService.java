package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseStatus;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryAlertStatus;
import com.ruoyi.rentalbusiness.enums.PlatformExecutionWorkbenchTaskType;
import com.ruoyi.rentalbusiness.mapper.PlatformExecutionWorkbenchMapper;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchTaskView;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchView;

/** 汇总租赁业务服务内需要平台继续跟进的执行面事项。 */
@Service
public class PlatformExecutionWorkbenchService
{
    private static final int TASK_LIMIT = 50;

    private final PlatformExecutionWorkbenchMapper mapper;

    public PlatformExecutionWorkbenchService(PlatformExecutionWorkbenchMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询履约、售后、物流和库存治理待办，不修改任何业务状态。 */
    public PlatformExecutionWorkbenchView overview()
    {
        PlatformExecutionWorkbenchView result = new PlatformExecutionWorkbenchView();
        result.setAvailable(Boolean.TRUE);
        result.setFulfillmentTaskCount(Integer.valueOf(mapper.countOpenFulfillmentTasks()));
        result.setAfterSalesTaskCount(Integer.valueOf(mapper.countOpenAfterSalesTasks()));
        result.setLogisticsTaskCount(Integer.valueOf(mapper.countOpenLogisticsTasks()));
        result.setInventoryAlertTaskCount(Integer.valueOf(mapper.countOpenInventoryAlertTasks()));

        List<PlatformExecutionWorkbenchTaskView> tasks = new ArrayList<PlatformExecutionWorkbenchTaskView>();
        append(tasks, mapper.selectFulfillmentTasks(TASK_LIMIT), PlatformExecutionWorkbenchTaskType.FULFILLMENT);
        append(tasks, mapper.selectAfterSalesTasks(TASK_LIMIT), PlatformExecutionWorkbenchTaskType.AFTER_SALES);
        append(tasks, mapper.selectLogisticsTasks(TASK_LIMIT), PlatformExecutionWorkbenchTaskType.LOGISTICS);
        append(tasks, mapper.selectInventoryAlertTasks(TASK_LIMIT), PlatformExecutionWorkbenchTaskType.INVENTORY_ALERT);
        result.setTasks(tasks);
        return result;
    }

    private void append(List<PlatformExecutionWorkbenchTaskView> target,
            List<PlatformExecutionWorkbenchTaskView> source, PlatformExecutionWorkbenchTaskType type)
    {
        if (source == null)
        {
            return;
        }
        for (PlatformExecutionWorkbenchTaskView task : source)
        {
            if (task != null)
            {
                task.setTaskStatusName(statusName(type, task.getTaskStatus()));
                target.add(task);
            }
        }
    }

    private String statusName(PlatformExecutionWorkbenchTaskType type, Integer status)
    {
        if (type == PlatformExecutionWorkbenchTaskType.FULFILLMENT)
        {
            return label(RentalCode.OrderStatus.values(), status, "未知订单状态");
        }
        if (type == PlatformExecutionWorkbenchTaskType.AFTER_SALES)
        {
            MerchantAfterSalesCaseStatus value = MerchantAfterSalesCaseStatus.fromCode(status);
            return value == null ? "未知售后状态" : value.getLabel();
        }
        if (type == PlatformExecutionWorkbenchTaskType.LOGISTICS)
        {
            return label(RentalCode.LogisticsRouteStatus.values(), status, "未知物流状态");
        }
        MerchantInventoryAlertStatus value = MerchantInventoryAlertStatus.fromCode(status);
        return value == null ? "未知告警状态" : value.getLabel();
    }

    private <T extends com.ruoyi.common.core.enums.NumericCode> String label(T[] values, Integer code,
            String fallback)
    {
        if (code != null)
        {
            for (T value : values)
            {
                if (value.getCode().equals(code))
                {
                    return value.getLabel();
                }
            }
        }
        return fallback;
    }
}
