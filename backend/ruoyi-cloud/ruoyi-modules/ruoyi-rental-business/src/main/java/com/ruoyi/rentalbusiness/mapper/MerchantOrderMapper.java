package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;

/** 商户订单查询数据访问。所有查询必须带租户、商户和软删除条件。 */
public interface MerchantOrderMapper
{
    /**
     * 按当前商户上下文分页查询订单工作台所需字段。
     *
     * @param tenantId 可信租户主键
     * @param merchantId 可信商户主键
     * @param query 已覆盖仓库范围的商户订单查询条件
     * @return 当前用户可见的订单查询行
     */
    List<MerchantOrderRow> selectMerchantOrderList(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantOrderQuery query);
}
