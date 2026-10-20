package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryQuery;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryUnitView;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;

/** 为周转市场提供已完成实物认证的逐件库存查询。 */
@Service
public class MerchantMarketInventoryService
{
    private static final int MAX_UNIT_COUNT = 5000;
    private final MerchantInventoryMapper mapper;

    public MerchantMarketInventoryService(MerchantInventoryMapper mapper)
    {
        this.mapper = mapper;
    }

    /**
     * 查询当前商户可绑定的市场库存。
     *
     * <p>认证条件由数据库事实统一判断：库存单元必须可用，并且已经保存实物照片。</p>
     */
    public List<MerchantMarketInventoryUnitView> eligible(MerchantMarketInventoryQuery query)
    {
        if (query == null || query.getTenantId() == null || query.getMemberId() == null)
        {
            throw new ServiceException("市场库存查询范围不能为空");
        }
        List<Long> unitIds = normalizeUnitIds(query.getUnitIds());
        List<Map<String, Object>> rows = mapper.selectMarketEligibleUnits(query.getTenantId(), query.getMemberId(),
                trim(query.getBrand()), trim(query.getModelName()), trim(query.getMemorySpec()), unitIds);
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<MerchantMarketInventoryUnitView> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows)
        {
            MerchantMarketInventoryUnitView view = new MerchantMarketInventoryUnitView();
            view.setUnitId(number(row.get("unitId")));
            view.setUnitCode(text(row.get("unitCode")));
            view.setSerialNumber(text(row.get("serialNumber")));
            view.setBrand(text(row.get("brand")));
            view.setModelName(text(row.get("modelName")));
            view.setMemorySpec(text(row.get("memorySpec")));
            view.setConditionLevel(integer(row.get("conditionLevel")));
            view.setWarehouseName(text(row.get("warehouseName")));
            result.add(view);
        }
        return result;
    }

    private List<Long> normalizeUnitIds(List<Long> unitIds)
    {
        if (unitIds == null || unitIds.isEmpty()) return Collections.emptyList();
        if (unitIds.size() > MAX_UNIT_COUNT) throw new ServiceException("单次市场上架最多绑定" + MAX_UNIT_COUNT + "台设备");
        List<Long> result = new ArrayList<>();
        for (Long unitId : unitIds)
        {
            if (unitId == null || unitId <= 0 || result.contains(unitId)) throw new ServiceException("市场库存设备选择不合法");
            result.add(unitId);
        }
        return result;
    }

    private String trim(String value) { return value == null || value.trim().isEmpty() ? null : value.trim(); }
    private String text(Object value) { return value == null ? null : value.toString(); }
    private Integer integer(Object value) { return value == null ? null : ((Number) value).intValue(); }
    private Long number(Object value) { return value == null ? null : ((Number) value).longValue(); }
}
