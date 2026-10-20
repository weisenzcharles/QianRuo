package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.merchant.api.RemoteRentalBusinessService;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryQuery;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryUnitView;

/** 商户周转市场库存选择服务，所有执行面库存查询通过内部契约完成。 */
@Service
public class MerchantMarketInventoryService
{
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    public MerchantMarketInventoryService(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /** 查询当前商户符合市场认证条件的逐件库存。 */
    public List<MerchantMarketInventoryUnitView> listEligible(String brand, String modelName, String memorySpec,
            List<Long> unitIds)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantMarketInventoryQuery query = new MerchantMarketInventoryQuery();
        query.setTenantId(MerchantContextUtils.getTenantId());
        query.setMemberId(MerchantContextUtils.getMemberId());
        query.setBrand(trim(brand));
        query.setModelName(trim(modelName));
        query.setMemorySpec(trim(memorySpec));
        query.setUnitIds(unitIds == null ? Collections.emptyList() : unitIds);
        R<List<MerchantMarketInventoryUnitView>> response = remoteRentalBusinessService.listMarketEligibleUnits(query,
                SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode())
        {
            throw new ServiceException(response == null ? "市场库存服务无响应" : response.getMsg());
        }
        return response.getData() == null ? Collections.emptyList() : response.getData();
    }

    private String trim(String value) { return value == null || value.trim().isEmpty() ? null : value.trim(); }
}
