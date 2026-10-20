package com.ruoyi.rentalbusiness.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.domain.MerchantInventoryForecastRecord;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryForecastRiskLevel;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.model.MerchantInventoryForecastQuery;
import com.ruoyi.rentalbusiness.model.MerchantInventoryForecastRowVO;

/** 商户库存未来预测服务，负责快照生成、权限收敛和风险中文化。 */
@Service
public class MerchantInventoryForecastService
{
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int FORECAST_DAYS = 30;
    private static final int MODEL_TYPE = 1;
    private static final int PACKAGE_TYPE = 2;
    private static final Long ALL_WAREHOUSES = 0L;
    private static final Long SYSTEM_OPERATOR_ID = 0L;
    private static final List<String> EXCLUDED_FORECAST_ORDER_STATUSES = List.of(
            String.valueOf(RentalCode.OrderStatus.COMPLETED.getCode()), "completed",
            String.valueOf(RentalCode.OrderStatus.CANCELLED.getCode()), "cancelled");

    private final MerchantInventoryMapper mapper;

    public MerchantInventoryForecastService(MerchantInventoryMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询当前商户可见的预测快照；受限仓库用户不会读取商户级汇总快照。 */
    public List<MerchantInventoryForecastRowVO> list(MerchantInventoryForecastQuery query)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantInventoryForecastQuery normalized = query == null ? new MerchantInventoryForecastQuery() : query;
        int forecastType = forecastType(normalized.mode);
        int days = normalizeDays(normalized.days);
        checkWarehouse(normalized.warehouseId);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        if (normalized.warehouseId != null)
        {
            return decorate(mapper.selectForecastSnapshots(tenantId, memberId, normalized.warehouseId,
                    forecastType, trim(normalized.brand), normalized.modelId, days));
        }
        if (MerchantContextUtils.hasAllWarehouseAccess())
        {
            return decorate(mapper.selectForecastSnapshots(tenantId, memberId, ALL_WAREHOUSES,
                    forecastType, trim(normalized.brand), normalized.modelId, days));
        }
        Set<Long> warehouseIds = MerchantContextUtils.getWarehouseIds();
        if (warehouseIds == null || warehouseIds.isEmpty())
        {
            return Collections.emptyList();
        }
        List<MerchantInventoryForecastRowVO> result = new ArrayList<>();
        for (Long warehouseId : warehouseIds)
        {
            result.addAll(mapper.selectForecastSnapshots(tenantId, memberId, warehouseId, forecastType,
                    trim(normalized.brand), normalized.modelId, days));
        }
        return decorate(result);
    }

    /** 手工刷新当前商户未来三十天预测，供页面和运营人员排障使用。 */
    @Transactional(rollbackFor = Exception.class)
    public int refreshCurrentMerchant()
    {
        MerchantContextUtils.requireMerchantApproved();
        return refreshMember(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                LocalDate.now(BUSINESS_ZONE));
    }

    /** 由统一调度任务刷新所有存在库存事实的商户。 */
    public int refreshAll(LocalDate baseDate)
    {
        LocalDate actualBaseDate = baseDate == null ? LocalDate.now(BUSINESS_ZONE) : baseDate;
        List<Map<String, Object>> scopes = mapper.selectForecastMemberScopes();
        if (scopes == null || scopes.isEmpty())
        {
            return 0;
        }
        int saved = 0;
        for (Map<String, Object> scope : scopes)
        {
            Long tenantId = number(scope == null ? null : scope.get("tenantId"));
            Long memberId = number(scope == null ? null : scope.get("memberId"));
            if (tenantId != null && memberId != null)
            {
                saved += refreshMember(tenantId, memberId, actualBaseDate);
            }
        }
        return saved;
    }

    /** 为一个商户独立删除并生成一轮快照，保证失败时不会混合两次计算结果。 */
    @Transactional(rollbackFor = Exception.class)
    public int refreshMember(Long tenantId, Long memberId, LocalDate baseDate)
    {
        if (tenantId == null || memberId == null)
        {
            return 0;
        }
        LocalDate actualBaseDate = baseDate == null ? LocalDate.now(BUSINESS_ZONE) : baseDate;
        LocalDate endDate = actualBaseDate.plusDays(FORECAST_DAYS - 1L);
        mapper.removeForecastSnapshots(tenantId, memberId, actualBaseDate, endDate, SYSTEM_OPERATOR_ID);

        LinkedHashSet<Long> warehouseScopes = new LinkedHashSet<>();
        warehouseScopes.add(ALL_WAREHOUSES);
        List<Long> warehouseIds = mapper.selectForecastWarehouseIds(tenantId, memberId);
        if (warehouseIds != null)
        {
            warehouseScopes.addAll(warehouseIds);
        }
        LocalDateTime generatedTime = LocalDateTime.now(BUSINESS_ZONE);
        List<MerchantInventoryForecastRecord> rows = new ArrayList<>();
        for (Long warehouseScope : warehouseScopes)
        {
            Long queryWarehouseId = ALL_WAREHOUSES.equals(warehouseScope) ? null : warehouseScope;
            rows.addAll(toRecords(tenantId, memberId, warehouseScope,
                    mapper.selectModelOverview(tenantId, memberId, queryWarehouseId, null, null, FORECAST_DAYS,
                            EXCLUDED_FORECAST_ORDER_STATUSES),
                    MODEL_TYPE, generatedTime));
            rows.addAll(toRecords(tenantId, memberId, warehouseScope,
                    mapper.selectPackageOverview(tenantId, memberId, queryWarehouseId, FORECAST_DAYS,
                            EXCLUDED_FORECAST_ORDER_STATUSES),
                    PACKAGE_TYPE, generatedTime));
        }
        if (!rows.isEmpty())
        {
            mapper.insertForecastSnapshots(rows);
        }
        return rows.size();
    }

    private List<MerchantInventoryForecastRecord> toRecords(Long tenantId, Long memberId, Long warehouseId,
            List<Map<String, Object>> sourceRows, int forecastType, LocalDateTime generatedTime)
    {
        if (sourceRows == null || sourceRows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<MerchantInventoryForecastRecord> result = new ArrayList<>(sourceRows.size());
        for (Map<String, Object> source : sourceRows)
        {
            if (source == null)
            {
                continue;
            }
            Long itemId = number(source.get(forecastType == MODEL_TYPE ? "modelId" : "packageId"));
            if (itemId == null)
            {
                continue;
            }
            int total = intValue(source.get("totalQuantity"));
            int available = Math.max(0, intValue(source.get("availableQuantity")));
            int orderable = source.containsKey("orderableQuantity")
                    ? Math.min(available, Math.max(0, intValue(source.get("orderableQuantity")))) : available;
            int occupied = Math.max(0, total - available);
            MerchantInventoryForecastRiskLevel risk = riskLevel(total, orderable);
            MerchantInventoryForecastRecord row = new MerchantInventoryForecastRecord();
            row.tenantId = tenantId;
            row.memberId = memberId;
            row.warehouseId = warehouseId;
            row.forecastType = forecastType;
            row.itemId = itemId;
            row.itemCode = text(source.get("itemCode"));
            row.itemName = defaultText(text(source.get("itemName")), "未命名对象");
            row.brand = text(source.get("brand"));
            row.snapshotDate = dateValue(source.get("forecastDate"));
            if (row.snapshotDate == null)
            {
                continue;
            }
            row.totalQuantity = total;
            row.occupiedQuantity = occupied;
            row.availableQuantity = available;
            row.orderableQuantity = orderable;
            row.riskLevel = risk.getCode();
            row.riskMessage = risk == MerchantInventoryForecastRiskLevel.NORMAL ? "库存充足"
                    : risk == MerchantInventoryForecastRiskLevel.LOW ? "预计可用库存偏低" : "预计无可用库存";
            row.generatedTime = generatedTime;
            row.createBy = SYSTEM_OPERATOR_ID;
            row.updateBy = SYSTEM_OPERATOR_ID;
            result.add(row);
        }
        return result;
    }

    private List<MerchantInventoryForecastRowVO> decorate(List<MerchantInventoryForecastRowVO> rows)
    {
        if (rows == null)
        {
            return Collections.emptyList();
        }
        for (MerchantInventoryForecastRowVO row : rows)
        {
            row.forecastTypeName = row.forecastType != null && row.forecastType == PACKAGE_TYPE ? "套餐" : "型号";
            MerchantInventoryForecastRiskLevel risk = MerchantInventoryForecastRiskLevel.fromCode(row.riskLevel);
            row.riskLevelName = risk == null ? "未知风险" : risk.getLabel();
        }
        return rows;
    }

    private MerchantInventoryForecastRiskLevel riskLevel(int total, int available)
    {
        if (available <= 0)
        {
            return MerchantInventoryForecastRiskLevel.UNAVAILABLE;
        }
        int lowThreshold = Math.max(1, (total + 9) / 10);
        return available <= lowThreshold ? MerchantInventoryForecastRiskLevel.LOW
                : MerchantInventoryForecastRiskLevel.NORMAL;
    }

    private int forecastType(String mode)
    {
        String normalized = mode == null ? "model" : mode.trim().toLowerCase(Locale.ROOT);
        if ("package".equals(normalized))
        {
            return PACKAGE_TYPE;
        }
        if (!"model".equals(normalized))
        {
            throw new ServiceException("库存预测类型不合法");
        }
        return MODEL_TYPE;
    }

    private int normalizeDays(Integer days)
    {
        int value = days == null ? FORECAST_DAYS : days;
        if (value != 3 && value != 5 && value != 7 && value != 15 && value != 30)
        {
            throw new ServiceException("预测天数必须为3天、5天、7天、15天或30天");
        }
        return value;
    }

    private void checkWarehouse(Long warehouseId)
    {
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
    }

    private LocalDate dateValue(Object value)
    {
        if (value instanceof LocalDate localDate)
        {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate)
        {
            return sqlDate.toLocalDate();
        }
        if (value == null)
        {
            return null;
        }
        try
        {
            return LocalDate.parse(String.valueOf(value).substring(0, 10));
        }
        catch (RuntimeException exception)
        {
            return null;
        }
    }

    private Long number(Object value)
    {
        return value instanceof Number number ? number.longValue() : null;
    }

    private int intValue(Object value)
    {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private String text(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String defaultText(String value, String fallback)
    {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }
}
