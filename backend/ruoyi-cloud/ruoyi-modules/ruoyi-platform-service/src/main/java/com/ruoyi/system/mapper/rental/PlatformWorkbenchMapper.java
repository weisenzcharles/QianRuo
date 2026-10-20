package com.ruoyi.system.mapper.rental;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformWorkbenchTaskVO;

/** 平台通用工作台待办查询数据访问组件。 */
public interface PlatformWorkbenchMapper
{
    int countOpenRiskTasks();

    int countOpenTicketTasks();

    int countPendingWithdrawalTasks();

    int countOpenInvoiceTasks();

    int countOpenMarketReportTasks();

    BigDecimal sumPendingWithdrawalAmount();

    List<PlatformWorkbenchTaskVO> selectRiskTasks(@Param("limit") Integer limit);

    List<PlatformWorkbenchTaskVO> selectTicketTasks(@Param("limit") Integer limit);

    List<PlatformWorkbenchTaskVO> selectWithdrawalTasks(@Param("limit") Integer limit);

    List<PlatformWorkbenchTaskVO> selectInvoiceTasks(@Param("limit") Integer limit);

    List<PlatformWorkbenchTaskVO> selectMarketReportTasks(@Param("limit") Integer limit);
}
