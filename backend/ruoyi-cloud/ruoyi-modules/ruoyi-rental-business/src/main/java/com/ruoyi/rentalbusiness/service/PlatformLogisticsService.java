package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.rentalbusiness.mapper.PlatformLogisticsMapper;
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.api.model.PlatformLogisticsCallView;
import com.ruoyi.system.api.model.PlatformLogisticsPageView;
import com.ruoyi.system.api.model.PlatformLogisticsQuery;
import com.ruoyi.system.api.model.PlatformLogisticsRouteView;
import com.ruoyi.system.api.model.PlatformLogisticsView;

/** 平台物流治理查询服务，负责跨商户路由状态和缓存明细展示。 */
@Service
public class PlatformLogisticsService
{
    private final PlatformLogisticsMapper mapper;
    private final MerchantLogisticsTrackingService trackingService;

    @org.springframework.beans.factory.annotation.Autowired
    public PlatformLogisticsService(PlatformLogisticsMapper mapper, MerchantLogisticsTrackingService trackingService)
    {
        this.mapper = mapper;
        this.trackingService = trackingService;
    }

    /** 保留查询单元测试使用的构造方式。 */
    public PlatformLogisticsService(PlatformLogisticsMapper mapper)
    {
        this(mapper, null);
    }

    /** 分页查询全平台物流台账，并把枚举代码转换成中文。 */
    public PlatformLogisticsPageView list(PlatformLogisticsQuery query)
    {
        PlatformLogisticsQuery normalized = query == null ? new PlatformLogisticsQuery() : query;
        normalized.setKeyword(trim(normalized.getKeyword()));
        normalized.setMerchantKeyword(trim(normalized.getMerchantKeyword()));
        validateDirection(normalized.getDirection());
        validateRouteStatus(normalized.getRouteStatus());
        int pageNum = normalized.getPageNum() == null || normalized.getPageNum().intValue() < 1
                ? 1 : normalized.getPageNum().intValue();
        int pageSize = normalized.getPageSize() == null || normalized.getPageSize().intValue() < 1
                ? 10 : Math.min(normalized.getPageSize().intValue(), 100);
        normalized.setPageNum(pageNum);
        normalized.setPageSize(pageSize);

        long total = mapper.countLogistics(normalized);
        List<PlatformLogisticsView> rows = total == 0L ? Collections.emptyList()
                : mapper.selectLogistics(normalized, (pageNum - 1) * pageSize, pageSize);
        decorate(rows);
        PlatformLogisticsPageView result = new PlatformLogisticsPageView();
        result.setRows(rows == null ? Collections.emptyList() : rows);
        result.setTotal(total);
        return result;
    }

    /** 查询平台物流详情及已落库路线节点。 */
    public PlatformLogisticsDetailView detail(Long logisticsId)
    {
        requirePositive(logisticsId, "物流唯一编号不合法");
        PlatformLogisticsView logistics = mapper.selectLogisticsById(logisticsId);
        if (logistics == null)
        {
            throw new ServiceException("物流记录不存在");
        }
        decorate(logistics);
        List<PlatformLogisticsRouteView> routes = mapper.selectRoutes(logistics.getTenantId(), logistics.getMemberId(),
                logisticsId);
        decorateRoutes(routes);
        List<PlatformLogisticsCallView> calls = mapper.selectProviderCalls(logistics.getTenantId(), logistics.getMemberId(),
                logisticsId);
        decorateCalls(calls);
        PlatformLogisticsDetailView result = new PlatformLogisticsDetailView();
        result.setLogistics(logistics);
        result.setRoutes(routes == null ? Collections.emptyList() : routes);
        result.setCalls(calls == null ? Collections.emptyList() : calls);
        return result;
    }

    /** 手工刷新平台选中的单条物流路线，不推进订单履约状态。 */
    public int refresh(Long logisticsId)
    {
        if (trackingService == null)
        {
            throw new ServiceException("物流刷新服务未初始化");
        }
        return trackingService.refreshRoute(logisticsId);
    }

    private void decorate(List<PlatformLogisticsView> rows)
    {
        if (rows != null)
        {
            for (PlatformLogisticsView row : rows)
            {
                decorate(row);
            }
        }
    }

    private void decorate(PlatformLogisticsView row)
    {
        if (row == null)
        {
            return;
        }
        row.setDirectionName(label(RentalCode.DeliveryDirection.values(), row.getDirection(), "未知方向"));
        row.setSourceTypeName(label(RentalCode.LogisticsSourceType.values(), row.getSourceType(), "未知来源"));
        row.setRouteStatusName(label(RentalCode.LogisticsRouteStatus.values(), row.getRouteStatus(), "未知状态"));
    }

    private void decorateRoutes(List<PlatformLogisticsRouteView> rows)
    {
        if (rows != null)
        {
            for (PlatformLogisticsRouteView row : rows)
            {
                if (row != null)
                {
                    row.setRouteStatusName(label(RentalCode.LogisticsRouteStatus.values(), row.getRouteStatus(), "未知状态"));
                }
            }
        }
    }

    private void decorateCalls(List<PlatformLogisticsCallView> rows)
    {
        if (rows == null)
        {
            return;
        }
        for (PlatformLogisticsCallView row : rows)
        {
            if (row == null)
            {
                continue;
            }
            row.setProviderName(providerLabel(row.getProvider()));
            row.setAvailableName(Integer.valueOf(1).equals(row.getAvailable()) ? "查询成功" : "查询失败");
            row.setRouteStatusName(label(RentalCode.LogisticsRouteStatus.values(), row.getRouteStatus(), "未知状态"));
            row.setErrorTypeName(errorTypeLabel(row.getErrorType()));
        }
    }

    private String providerLabel(String provider)
    {
        if ("sf".equalsIgnoreCase(provider))
        {
            return "顺丰";
        }
        if ("kuaidi100".equalsIgnoreCase(provider))
        {
            return "快递100";
        }
        return StringUtils.isBlank(provider) ? "未知供应商" : provider;
    }

    private String errorTypeLabel(String errorType)
    {
        if (StringUtils.isBlank(errorType))
        {
            return "无异常";
        }
        if ("rate_limited".equalsIgnoreCase(errorType))
        {
            return "触发限流";
        }
        if ("provider_unavailable".equalsIgnoreCase(errorType))
        {
            return "供应商未返回有效轨迹";
        }
        return "接口异常";
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

    private void validateDirection(Integer value)
    {
        if (value != null && label(RentalCode.DeliveryDirection.values(), value, null) == null)
        {
            throw new ServiceException("物流方向不合法");
        }
    }

    private void validateRouteStatus(Integer value)
    {
        if (value != null && label(RentalCode.LogisticsRouteStatus.values(), value, null) == null)
        {
            throw new ServiceException("物流状态不合法");
        }
    }

    private static String trim(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static void requirePositive(Long value, String message)
    {
        if (value == null || value.longValue() <= 0L)
        {
            throw new ServiceException(message);
        }
    }
}
