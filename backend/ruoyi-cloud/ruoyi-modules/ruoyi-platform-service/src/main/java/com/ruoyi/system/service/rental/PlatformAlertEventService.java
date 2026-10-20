package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.enums.PlatformAlertEventLevel;
import com.ruoyi.system.enums.PlatformAlertEventStatus;
import com.ruoyi.system.enums.PlatformAlertEventType;
import com.ruoyi.system.mapper.rental.PlatformAlertEventMapper;
import com.ruoyi.system.api.model.PlatformExternalInterfaceAlertCommand;
import com.ruoyi.system.model.rental.PlatformAlertEventQuery;
import com.ruoyi.system.model.rental.PlatformAlertEventVO;

/**
 * 平台运维告警事件服务。
 *
 * <p>当前先接入调度失败日志，统一落为可确认、可解除的告警事件；支付、短信、存储和外部接口服务后续
 * 只需要复用同一事件模型即可，不再为每类异常重复设计处理状态。</p>
 */
@Service
public class PlatformAlertEventService
{
    private static final int MAX_REMARK_LENGTH = 500;

    private final PlatformAlertEventMapper mapper;

    public PlatformAlertEventService(PlatformAlertEventMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询告警列表，并把最近 24 小时调度失败同步为持久化事件。 */
    public List<PlatformAlertEventVO> list(PlatformAlertEventQuery query)
    {
        if (mapper == null)
            throw new ServiceException("平台运维告警服务暂不可用");
        synchronizeScheduledTaskAlertsInternal();
        List<PlatformAlertEventVO> rows = mapper.selectList(query == null ? new PlatformAlertEventQuery() : query);
        if (rows == null || rows.isEmpty())
            return Collections.emptyList();
        for (PlatformAlertEventVO row : rows)
            decorate(row);
        return rows;
    }

    /**
     * 同步最近 24 小时调度失败记录。
     *
     * <p>页面查询会调用本方法保证即时性，定时任务也会调用本方法保证告警不依赖页面访问。</p>
     *
     * @return 本次发现的调度失败来源数量
     */
    public int synchronizeScheduledTaskAlerts()
    {
        if (mapper == null)
            throw new ServiceException("平台运维告警服务暂不可用");
        return synchronizeScheduledTaskAlertsInternal();
    }

    /** 写入或刷新外部接口告警，供业务服务上报供应商不可用等基础设施异常。 */
    public void upsertExternalInterfaceAlert(PlatformExternalInterfaceAlertCommand command)
    {
        if (mapper == null)
            throw new ServiceException("平台运维告警服务暂不可用");
        if (command == null || isBlank(command.getAlertEventKey()))
            throw new ServiceException("外部接口告警业务键不能为空");
        PlatformAlertEventLevel level = PlatformAlertEventLevel.fromCode(command.getAlertLevel());
        if (level == null)
            throw new ServiceException("外部接口告警等级不合法");
        String title = limited(command.getTitle(), 200, "外部接口调用异常");
        String detail = limited(command.getDetail(), 1000, "外部接口调用异常");
        String sourceKey = limited(command.getSourceKey(), 255, null);
        mapper.upsertExternalInterfaceAlert(limited(command.getAlertEventKey(), 255, null),
                Integer.valueOf(level.getCode()), title, detail, sourceKey, SecurityUtils.getUserId());
    }

    /** 外部接口恢复后解除对应告警；重复解除保持幂等。 */
    public void resolveExternalInterfaceAlert(String alertEventKey)
    {
        if (mapper == null)
            throw new ServiceException("平台运维告警服务暂不可用");
        if (isBlank(alertEventKey))
            throw new ServiceException("外部接口告警业务键不能为空");
        mapper.resolveExternalInterfaceAlert(limited(alertEventKey, 255, null), SecurityUtils.getUserId(),
                "外部接口已恢复");
    }

    /** 确认待处理告警，已确认或已解除的事件不重复变更。 */
    public void acknowledge(Long alertEventId)
    {
        PlatformAlertEventVO event = requiredEvent(alertEventId);
        PlatformAlertEventStatus status = PlatformAlertEventStatus.fromCode(event.getEventStatus());
        if (status == PlatformAlertEventStatus.ACKNOWLEDGED)
            return;
        if (status == PlatformAlertEventStatus.RESOLVED)
            throw new ServiceException("运维告警已经解除");
        if (mapper.acknowledge(alertEventId, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("确认运维告警失败，请刷新后重试");
    }

    /** 解除当前待处理或已确认的告警。 */
    public void resolve(Long alertEventId, String remark)
    {
        PlatformAlertEventVO event = requiredEvent(alertEventId);
        if (PlatformAlertEventStatus.fromCode(event.getEventStatus()) == PlatformAlertEventStatus.RESOLVED)
            return;
        String normalizedRemark = remark == null ? "平台人工解除" : remark.trim();
        if (normalizedRemark.isEmpty())
            normalizedRemark = "平台人工解除";
        if (normalizedRemark.length() > MAX_REMARK_LENGTH)
            normalizedRemark = normalizedRemark.substring(0, MAX_REMARK_LENGTH);
        if (mapper.resolve(alertEventId, normalizedRemark, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("解除运维告警失败，请刷新后重试");
    }

    private PlatformAlertEventVO requiredEvent(Long alertEventId)
    {
        if (alertEventId == null || alertEventId.longValue() <= 0L)
            throw new ServiceException("运维告警事件唯一编号不合法");
        PlatformAlertEventVO event = mapper.selectById(alertEventId);
        if (event == null)
            throw new ServiceException("运维告警事件不存在或已删除");
        return event;
    }

    private int synchronizeScheduledTaskAlertsInternal()
    {
        List<Map<String, Object>> sources = mapper.selectRecentFailedJobs();
        List<String> activeKeys = new ArrayList<String>();
        if (sources != null)
        {
            for (Map<String, Object> source : sources)
            {
                String alertKey = text(source, "alertKey", "alert_key");
                if (alertKey == null || alertKey.isEmpty())
                    continue;
                activeKeys.add(alertKey);
                mapper.upsertScheduledTaskAlert(alertKey,
                        textOrDefault(source, "title", "title", "调度任务执行失败"),
                        textOrDefault(source, "detail", "detail", "调度任务执行失败"),
                        text(source, "sourceKey", "source_key"), number(source, "occurrenceCount", "occurrence_count"),
                        SecurityUtils.getUserId());
            }
        }
        mapper.resolveMissingScheduledTaskAlerts(activeKeys, SecurityUtils.getUserId());
        return sources == null ? 0 : sources.size();
    }

    private void decorate(PlatformAlertEventVO row)
    {
        if (row == null)
            return;
        PlatformAlertEventType type = PlatformAlertEventType.fromCode(row.getAlertType());
        PlatformAlertEventLevel level = PlatformAlertEventLevel.fromCode(row.getAlertLevel());
        PlatformAlertEventStatus status = PlatformAlertEventStatus.fromCode(row.getEventStatus());
        row.setAlertTypeName(type == null ? "未知类型" : type.getLabel());
        row.setAlertLevelName(level == null ? "未知等级" : level.getLabel());
        row.setEventStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private String textOrDefault(Map<String, Object> row, String primaryKey, String fallbackKey, String defaultValue)
    {
        String value = text(row, primaryKey, fallbackKey);
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    private String text(Map<String, Object> row, String primaryKey, String fallbackKey)
    {
        if (row == null)
            return null;
        Object value = row.get(primaryKey);
        if (value == null)
            value = row.get(fallbackKey);
        if (value == null)
            return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Long number(Map<String, Object> row, String primaryKey, String fallbackKey)
    {
        if (row == null)
            return Long.valueOf(0L);
        Object value = row.get(primaryKey);
        if (value == null)
            value = row.get(fallbackKey);
        return value instanceof Number ? Long.valueOf(((Number) value).longValue()) : Long.valueOf(0L);
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private String limited(String value, int maxLength, String defaultValue)
    {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty())
            return defaultValue;
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }
}
