package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.mapper.PlatformMerchantBusinessMapper;
import com.ruoyi.system.api.model.PlatformMerchantBusinessView;
import com.ruoyi.system.api.model.PlatformMerchantGovernanceView;

/** rental-business 平台商户经营明细聚合查询服务。 */
@Service
public class PlatformMerchantBusinessQueryService
{
    private static final int MAX_DETAIL_ROWS = 200;

    private final PlatformMerchantBusinessMapper businessMapper;

    public PlatformMerchantBusinessQueryService(PlatformMerchantBusinessMapper businessMapper)
    {
        this.businessMapper = businessMapper;
    }

    /**
     * 查询指定商户的经营明细。
     *
     * <p>平台端使用该只读接口查看商户数据，明细按类别限制返回数量，避免一次打开详情拖取
     * 全量商品或库存；需要完整管理时仍应进入对应的商户业务服务。</p>
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @return 商户经营明细聚合视图
     */
    public PlatformMerchantBusinessView get(Long tenantId, Long memberId)
    {
        requireScope(tenantId, memberId);
        PlatformMerchantBusinessView result = new PlatformMerchantBusinessView();
        result.setWarehouses(safe(businessMapper.selectWarehouses(tenantId, memberId, MAX_DETAIL_ROWS)));
        result.setProducts(safe(businessMapper.selectProducts(tenantId, memberId, MAX_DETAIL_ROWS)));
        result.setPackages(safe(businessMapper.selectPackages(tenantId, memberId, MAX_DETAIL_ROWS)));
        result.setInventories(safe(businessMapper.selectInventories(tenantId, memberId, MAX_DETAIL_ROWS)));
        return result;
    }

    /** 查询指定商户的履约、物流和风险治理汇总。 */
    public PlatformMerchantGovernanceView getGovernance(Long tenantId, Long memberId)
    {
        requireScope(tenantId, memberId);
        PlatformMerchantGovernanceView result = businessMapper.selectGovernance(tenantId, memberId);
        return result == null ? new PlatformMerchantGovernanceView() : result;
    }

    private void requireScope(Long tenantId, Long memberId)
    {
        if (tenantId == null || tenantId.longValue() <= 0L || memberId == null || memberId.longValue() <= 0L)
        {
            throw new ServiceException("商户经营明细范围不合法");
        }
    }

    private <T> List<T> safe(List<T> value)
    {
        return value == null ? Collections.emptyList() : value;
    }
}
