package com.ruoyi.system.mapper.rental;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformAlertEventQuery;
import com.ruoyi.system.model.rental.PlatformAlertEventVO;

/** 平台运维告警事件数据访问组件。 */
public interface PlatformAlertEventMapper
{
    List<PlatformAlertEventVO> selectList(@Param("query") PlatformAlertEventQuery query);

    /** 查询当前仍待确认或已确认未解除的告警数量，供经营风控总览使用。 */
    int countOpenEvents();

    /** 查询近 24 小时失败的调度任务，供告警事件同步使用。 */
    List<Map<String, Object>> selectRecentFailedJobs();

    int upsertScheduledTaskAlert(@Param("alertEventKey") String alertEventKey,
            @Param("title") String title, @Param("detail") String detail,
            @Param("sourceKey") String sourceKey, @Param("occurrenceCount") Long occurrenceCount,
            @Param("operator") Long operator);

    /** 写入或刷新容量告警，使用业务键保证同一容量范围幂等。 */
    int upsertStorageAlert(@Param("alertEventKey") String alertEventKey, @Param("alertType") Integer alertType,
            @Param("alertLevel") Integer alertLevel, @Param("title") String title,
            @Param("detail") String detail, @Param("sourceKey") String sourceKey,
            @Param("operator") Long operator);

    /** 容量恢复正常后解除对应告警。 */
    int resolveStorageAlert(@Param("alertEventKey") String alertEventKey, @Param("operator") Long operator,
            @Param("remark") String remark);

    /** 写入或刷新外部接口告警，告警类型固定为外部接口。 */
    int upsertExternalInterfaceAlert(@Param("alertEventKey") String alertEventKey,
            @Param("alertLevel") Integer alertLevel, @Param("title") String title,
            @Param("detail") String detail, @Param("sourceKey") String sourceKey,
            @Param("operator") Long operator);

    /** 外部接口恢复后解除告警。 */
    int resolveExternalInterfaceAlert(@Param("alertEventKey") String alertEventKey,
            @Param("operator") Long operator, @Param("remark") String remark);

    /** 将超过观察窗口且本次未发现的调度告警标记为已解除。 */
    int resolveMissingScheduledTaskAlerts(@Param("alertEventKeys") List<String> alertEventKeys,
            @Param("operator") Long operator);

    PlatformAlertEventVO selectById(@Param("alertEventId") Long alertEventId);

    int acknowledge(@Param("alertEventId") Long alertEventId, @Param("operator") Long operator);

    int resolve(@Param("alertEventId") Long alertEventId, @Param("remark") String remark,
            @Param("operator") Long operator);
}
