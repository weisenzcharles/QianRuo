package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformExecutionWorkbenchTaskView;

/** 平台执行面待办查询数据访问组件。 */
public interface PlatformExecutionWorkbenchMapper
{
    int countOpenFulfillmentTasks();

    int countOpenAfterSalesTasks();

    int countOpenLogisticsTasks();

    int countOpenInventoryAlertTasks();

    List<PlatformExecutionWorkbenchTaskView> selectFulfillmentTasks(@Param("limit") Integer limit);

    List<PlatformExecutionWorkbenchTaskView> selectAfterSalesTasks(@Param("limit") Integer limit);

    List<PlatformExecutionWorkbenchTaskView> selectLogisticsTasks(@Param("limit") Integer limit);

    List<PlatformExecutionWorkbenchTaskView> selectInventoryAlertTasks(@Param("limit") Integer limit);
}
