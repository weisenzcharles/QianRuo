package com.ruoyi.merchant.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.merchant.api.RemoteRentalBusinessService;
import com.ruoyi.merchant.api.model.MerchantWarehouseView;
import com.ruoyi.merchant.domain.MerchantWarehouse;

/**
 * 商户服务访问租赁业务服务仓库数据的应用网关。
 *
 * <p>商户服务只从可信登录上下文读取租户和商户主键，不接受浏览器提交的归属字段，
 * 并且不直接访问 {@code ry_business} 数据库。</p>
 */
@Service
public class RentalBusinessWarehouseGateway
{
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    /** 注入租赁业务服务内部 Feign 契约。 */
    public RentalBusinessWarehouseGateway(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /**
     * 查询当前可信商户的启用仓库，并转换为商户服务自己的领域摘要对象。
     *
     * @return 当前商户启用仓库集合
     */
    public List<MerchantWarehouse> listEnabledWarehouses()
    {
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        R<List<MerchantWarehouseView>> response = remoteRentalBusinessService.listWarehouses(tenantId, memberId,
                Integer.valueOf(0), SecurityConstants.INNER);
        List<MerchantWarehouseView> data = require(response);
        if (data == null || data.isEmpty())
        {
            return Collections.emptyList();
        }
        List<MerchantWarehouse> result = new ArrayList<MerchantWarehouse>(data.size());
        for (MerchantWarehouseView source : data)
        {
            MerchantWarehouse target = new MerchantWarehouse();
            target.setWarehouseId(source.getWarehouseId());
            target.setWarehouseCode(source.getWarehouseCode());
            target.setWarehouseName(source.getWarehouseName());
            result.add(target);
        }
        return result;
    }

    /**
     * 统一解包租赁业务服务响应，远程失败时拒绝返回空列表，避免误导商户端权限判断。
     *
     * @param response 租赁业务服务响应
     * @return 仓库传输对象集合
     */
    private List<MerchantWarehouseView> require(R<List<MerchantWarehouseView>> response)
    {
        if (response == null || !R.isSuccess(response))
        {
            throw new ServiceException(response == null ? "租赁业务服务无响应" : response.getMsg());
        }
        return response.getData();
    }
}
