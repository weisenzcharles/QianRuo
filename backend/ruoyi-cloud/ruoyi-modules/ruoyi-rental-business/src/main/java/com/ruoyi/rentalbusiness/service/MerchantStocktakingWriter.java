package com.ruoyi.rentalbusiness.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.enums.MerchantStocktakingMode;
import com.ruoyi.rentalbusiness.mapper.MerchantStocktakingMapper;

/** 在单个数据库事务内建立盘点单和账面快照。 */
@Service
public class MerchantStocktakingWriter
{
    private final MerchantStocktakingMapper mapper;

    public MerchantStocktakingWriter(MerchantStocktakingMapper mapper)
    {
        this.mapper = mapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(Long tenantId, Long memberId, Long warehouseId, String stocktakingNo,
            LocalDate stocktakingDate, Integer mode, Integer scope, Long classId, Long modelId,
            String scopeLabel, Integer expectedQuantity, Long operatorId, String remark)
    {
        if (mapper.insertStocktaking(tenantId, memberId, warehouseId, stocktakingNo, stocktakingDate,
                mode, scope, classId, modelId, scopeLabel, expectedQuantity, operatorId, remark) != 1)
        {
            throw new ServiceException("创建盘点单失败");
        }
        Long stocktakingId = mapper.selectLastInsertId();
        if (stocktakingId == null)
        {
            throw new ServiceException("创建盘点单失败");
        }
        if (mode == MerchantStocktakingMode.MANUAL.getDatabaseValue()
                && mapper.insertManualSnapshot(tenantId, memberId, stocktakingId, warehouseId, scope, classId,
                        modelId) < 0)
        {
            throw new ServiceException("生成账面库存快照失败");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stocktakingId", stocktakingId);
        result.put("stocktakingNo", stocktakingNo);
        return result;
    }
}
